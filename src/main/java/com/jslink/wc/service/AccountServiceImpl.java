package com.jslink.wc.service;

import com.jslink.wc.exception.ForbiddenException;
import com.jslink.wc.exception.UnprocessableEntityException;
import com.jslink.wc.pojo.Account;
import com.jslink.wc.pojo.Dict;
import com.jslink.wc.pojo.OrgType;
import com.jslink.wc.pojo.Works;
import com.jslink.wc.repository.AccountRepository;
import com.jslink.wc.repository.DictRepository;
import com.jslink.wc.repository.OrgTypeRepository;
import com.jslink.wc.repository.WorksRepository;
import com.jslink.wc.requestbody.UpdateAccountBody;
import com.jslink.wc.responsebody.AccountBody;
import com.jslink.wc.responsebody.PageResult;
import com.jslink.wc.util.Constants;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccountServiceImpl implements AccountService, UserDetailsService {
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private DictRepository dictRepository;
    @Autowired
    private OrgTypeRepository orgTypeRepository;
    @Autowired
    private WorksRepository worksRepository;
    @Override
    public PageResult<Account> getAccounts(String user, String name, String account, Integer orgTypeId, Integer type, int page, int pageSize) {
        Account curAcc = accountRepository.findByAccount(user);
        if (curAcc == null)
            throw new ForbiddenException("角色权限不足");
        if (curAcc.getType() == Constants.ACCOUNT_TYPE_UNIT2){
            List<Account> r = new ArrayList<>();
            r.add(curAcc);
            return new PageResult<>(r, 1, 1);
        }
        if (curAcc.getType() == Constants.ACCOUNT_TYPE_UNIT1){
            //查找推荐账户下的上传账户
            List<Account> repAccs = accountRepository.findByParentAccountId(curAcc.getId());
            repAccs.add(0, curAcc);
            return new PageResult<>(repAccs, page, repAccs.size());
        }

        Pageable pageable = PageRequest.of(page-1, pageSize);
        List<Specification<Account>> specs = new ArrayList<>();
        if (name != null && name.trim().length() > 0){
            specs.add((Specification<Account>) (root, query, cb) -> cb.like(root.get("name"), "%" + name + "%"));
        }
        if (account != null && account.trim().length() > 0){
            specs.add((Specification<Account>) (root, query, cb) -> cb.like(root.get("account"), "%" + account + "%"));
        }
        if (orgTypeId != null){
            specs.add((Specification<Account>) (root, query, cb) -> cb.equal(root.get("orgTypeId"), orgTypeId));
        }
        if (type != null){
            specs.add((Specification<Account>) (root, query, cb) -> cb.equal(root.get("type"), type));
        }
        Page<Account> accounts;
        if (specs.isEmpty())
            accounts = accountRepository.findAll(pageable);
        else {
            Specification<Account> spec = specs.get(0);
            for (int i = 1; i < specs.size(); i++) {
                spec = spec.and(specs.get(i));
            }
            accounts = accountRepository.findAll(spec, pageable);
        }
        List<Account> accs = new ArrayList<>();
        accs.addAll(accounts.getContent());
        return new PageResult(accs, page, accounts.getTotalElements());
    }

    /**
     * 创建帐号, 前端不传递type, 根据操作人的权限, 按下面方式创建
     *          管理员能创建'推荐单位' 帐号;
     *          '管理员'帐号手工创建
     *          '推荐单位'能创建'申报单位';
     * @param userName
     * @param account
     * @return
     */
    @Override
    public Account save(String userName, Account account) {
        Account user = accountRepository.findByAccount(userName);
        if (user == null){
            throw new ForbiddenException();
        }
        if (user.getType() == Constants.ACCOUNT_TYPE_UNIT2) throw new ForbiddenException("无权限");
        if (user.getType() == Constants.ACCOUNT_TYPE_ADMIN){
            account.setType(Constants.ACCOUNT_TYPE_UNIT1);
        }
        if (user.getType() == Constants.ACCOUNT_TYPE_UNIT1){
            account.setType(Constants.ACCOUNT_TYPE_UNIT2);
            account.setParentAccountId(user.getId());
            account.setOrgTypeId(user.getOrgTypeId());
        }
        //创建申报单位账号时, 要检查数量
        if (account.getType() == Constants.ACCOUNT_TYPE_UNIT2 && !verifyAccountQuantities(user, account.getPermission()))
            throw new ForbiddenException("帐号达到最大数目");
        //检查名称是否冲突
        Account tempacc = accountRepository.findByAccount(account.getAccount());
        if (tempacc != null){
            throw new UnprocessableEntityException("帐号名称已被占用");
        }
        if (account.getCreateDate() == null)
            account.setCreateDate(new Date());
        //密码加密
        account.setPassword(md5String(account.getPassword()));
        return accountRepository.save( account);
    }

    @Override
    public Account update(String user, UpdateAccountBody body) {
        Account operator = accountRepository.findByAccount(user); //操作员
        //这里能修改的字段只有下面几项.
        Account account = accountRepository.findById(body.getId()).get(); //要修改的对象

        //推荐单位修改申报单位账号时, 要检查可以提交的类型数量
        //但是申报单位自己修改时, permission不会传递过来, 就不需要检查
        //如果修改的内容不涉及"permission"字段, 不做这一步检查
        if (body.getPermission() != null && !body.getPermission().equals(account.getPermission()))
            if (operator.getType() == Constants.ACCOUNT_TYPE_UNIT1 && account.getType() == Constants.ACCOUNT_TYPE_UNIT2 && !verifyAccountQuantities(operator, body.getPermission()))
                throw new ForbiddenException("帐号达到最大数目");


        account.setName(body.getName());
        account.setAccount(body.getAccount());
        account.setPhone(body.getPhone());
        if (account.getType() == Constants.ACCOUNT_TYPE_UNIT1){
            //推荐单位修改组织机构时, 要把自身的覆盖下属"申报单位"
            if (body.getOrgTypeId() != account.getOrgTypeId()){
                List<Account> tjdws = accountRepository.findByParentAccountId(account.getId());
                tjdws.stream().forEach(tjdw -> {
                    tjdw.setOrgTypeId(body.getOrgTypeId());
                    accountRepository.save(tjdw);
                });
            }
            account.setOrgTypeId(body.getOrgTypeId());
            account.setEmail(body.getEmail());
            account.setContactPerson(body.getContactPerson());
        }
        if (operator.getType() != Constants.ACCOUNT_TYPE_UNIT2) {
            account.setPermission(body.getPermission());//申报单位不能修改申报类型
            account.setQuantityBrand(body.getQuantityBrand());
            account.setQuantityMgmtIndividual(body.getQuantityMgmtIndividual());
            account.setQuantityMgmtOrg(body.getQuantityMgmtOrg());
            account.setQuantityPeople(body.getQuantityPeople());
            account.setQuantityWorks(body.getQuantityWorks());
        }
        account = accountRepository.save(account);
        return account;
    }

    /**
     * 校验可以创建的申报单位数量, 根据"申报类型"来确定数目
     * @return
     */
    private boolean verifyAccountQuantities(Account operator, String permission){
        List<Account> sbAccounts = accountRepository.findByParentAccountId(operator.getId());
        //根据创建类型过滤已经存在的账户数量
        sbAccounts = sbAccounts.stream().filter(a -> a.getPermission().equals(permission)).collect(Collectors.toList());

        if (permission.equals(Constants.ACCOUNT_PERMISSION_BRAND) && sbAccounts.size() >= operator.getQuantityBrand()){
            return false;
        }
        if (permission.equals(Constants.ACCOUNT_PERMISSION_PEOPLE) && sbAccounts.size() >= operator.getQuantityPeople()){
            return false;
        }
        if (permission.equals(Constants.ACCOUNT_PERMISSION_WORKS) && sbAccounts.size() >= operator.getQuantityWorks()){
            return false;
        }
        if (permission.equals(Constants.ACCOUNT_PERMISSION_MGMTORG) && sbAccounts.size() >= operator.getQuantityMgmtOrg()){
            return false;
        }
        if (permission.equals(Constants.ACCOUNT_PERMISSION_MGMTINDIVIDUAL) && sbAccounts.size() >= operator.getQuantityMgmtIndividual()){
            return false;
        }
        return true;
    }

    @Override
    public Account login(String name, String password) throws Exception {
        Account account = accountRepository.findByAccount(name);
        if (account == null || account.getPassword() == null || !account.getPassword().equals(md5String(password)))
            throw new Exception("用户名密码不匹配");
        return account;
    }

    @Override
    public Account getAccount(String name) {
        return accountRepository.findByAccount(name);
    }

    /**
     * 管理员可以修改任何人密码
     * 推荐单位可以修改自己和下属帐号密码
     * 申报单位只能修改自己密码
     * @param user
     * @param id
     * @param password
     * @return
     */
    @Override
    public Account updatePassword(String user, Integer id, String password) {
        Account oper = accountRepository.findByAccount(user);
        Account account = accountRepository.findById(id).get();
        boolean allow = false;
        if(oper.getType() == Constants.ACCOUNT_TYPE_ADMIN){
            allow = true;
        } else if (oper.getType() == Constants.ACCOUNT_TYPE_UNIT1){
            if (oper.getId().equals(id) || account.getParentAccountId().equals(oper.getId()))
                allow = true;
        } else if (oper.getType() == Constants.ACCOUNT_TYPE_UNIT2){
            if (oper.getId().equals(id))
                allow = true;
        }
        if (!allow)
            throw new ForbiddenException("无权限操作该帐号");

        account.setPassword(md5String(password));
        account = accountRepository.save(account);
        return account;
    }

    @Override
    public AccountBody getTjdwAccount(int worksId) {
        Works works = worksRepository.findById(worksId).get();
        Account sbdw = works.getAccount();
        Account tjdw = accountRepository.findById(sbdw.getParentAccountId()).get();
        AccountBody ab = new AccountBody();
        ab.setType(tjdw.getType());
        ab.setName(tjdw.getName());
        ab.setPhone(tjdw.getPhone());
        ab.setUserid(tjdw.getId());
        ab.setOrgTypeId(tjdw.getOrgTypeId());
        ab.setOrgTypeName(orgTypeRepository.findById(tjdw.getOrgTypeId()).get().getName());
        return ab;
    }

    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        Account account = accountRepository.findByAccount(name);
        if (account == null) return null;
        return new User(account.getAccount(), account.getPassword(), new ArrayList<>());
    }

    public String md5String(String password) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new UnprocessableEntityException(e.getMessage());
        }
        byte[] hashInBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.println(new AccountServiceImpl().md5String("admin"));
    }
}
