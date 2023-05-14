package com.jslink.wc.service;

import com.jslink.wc.pojo.*;
import com.jslink.wc.repository.*;
import com.jslink.wc.responsebody.*;
import com.jslink.wc.util.Constants;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class StatsServiceImpl implements StatsService{

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private OutstandingPeopleRepository outstandingPeopleRepository;
    @Autowired
    private PopsciMgmtRepository popsciMgmtRepository;
    @Autowired
    private WorksRepository worksRepository;
    @Override
    public PageResult<StatsBody> getStats() {
        int DRAFT = Constants.WORKS_STATUS_DRAFT;
        int SUBMIT = Constants.WORKS_STATUS_SUBMIT;
        int INDIVIDUAL = Constants.POPSCI_APPLYTYPE_INDIVIDUAL;
        int ORG = Constants.POPSCI_APPLYTYPE_ORG;

        List<Account> accounts = accountRepository.findAll();
        List<Works> works = worksRepository.findAll();
        List<Brand> brands = brandRepository.findAll();
        List<PopsciMgmt> popsciMgmts = popsciMgmtRepository.findAll();
        List<OutstandingPeople> people = outstandingPeopleRepository.findAll();
        List<AccountBody> bAccounts = accounts.stream().map(a -> {
            AccountBody ab = new AccountBody();
            BeanUtils.copyProperties(a, ab);
            return ab;
        }).collect(Collectors.toList());
        List<BrandBody> bBrands = brands.stream().map(b -> {
            BrandBody bb = new BrandBody();
            BeanUtils.copyProperties(b, bb);
            bb.setAccountId(b.getAccount().getId());
            return bb;
        }).collect(Collectors.toList());
        List<WorksBody> bWorks = works.stream().map(w -> {
            WorksBody wb = new WorksBody();
            BeanUtils.copyProperties(w, wb);
            wb.setAccountId(w.getAccount().getId());
            return wb;
        }).collect(Collectors.toList());
        List<PopsciMgmtBody> bPopsciMgmts = popsciMgmts.stream().map(m -> {
            PopsciMgmtBody pb = new PopsciMgmtBody();
            BeanUtils.copyProperties(m, pb);;
            pb.setAccountId(m.getAccount().getId());
            return pb;
        }).collect(Collectors.toList());
        List<OutstandingPeopleBody> bPeople = people.stream().map(p -> {
            OutstandingPeopleBody pb = new OutstandingPeopleBody();
            BeanUtils.copyProperties(p, pb);
            pb.setAccountId(p.getAccount().getId());
            return pb;
        }).collect(Collectors.toList());
        StatsBody sbTotal = new StatsBody(1, "总数");
        StatsBody sbDraft = new StatsBody(2, "草稿");
        StatsBody sbSubmit = new StatsBody(3, "已提交");
        StatsBody sbNoReccForm = new StatsBody(4, "无推荐表");
        //account
        sbTotal.setTjdws(bAccounts.stream().filter(a -> a.getType() == Constants.ACCOUNT_TYPE_UNIT1).collect(Collectors.toList()));
        sbTotal.setSbdws(bAccounts.stream().filter(a -> a.getType() == Constants.ACCOUNT_TYPE_UNIT2).collect(Collectors.toList()));
        //brand
        sbTotal.setBrands(bBrands);
        sbDraft.setBrands(bBrands.stream().filter(b -> b.getStatus() == DRAFT).collect(Collectors.toList()));
        sbSubmit.setBrands(bBrands.stream().filter(b -> b.getStatus() == SUBMIT).collect(Collectors.toList()));
        sbNoReccForm.setBrands(bBrands.stream().filter(b -> b.getStatus() == SUBMIT && b.getReccFormFileUrl() == null).collect(Collectors.toList()));
        //mgmt-person
        sbTotal.setMgmtPersons(bPopsciMgmts.stream().filter(m -> m.getApplyType() == INDIVIDUAL).collect(Collectors.toList()));
        sbDraft.setMgmtPersons(bPopsciMgmts.stream().filter(m -> m.getApplyType() == INDIVIDUAL && m.getStatus() == DRAFT).collect(Collectors.toList()));
        sbSubmit.setMgmtPersons(bPopsciMgmts.stream().filter(m -> m.getApplyType() == INDIVIDUAL && m.getStatus() == SUBMIT).collect(Collectors.toList()));
        sbNoReccForm.setMgmtPersons(bPopsciMgmts.stream().filter(m -> m.getApplyType() == INDIVIDUAL && m.getStatus() == SUBMIT && m.getReccFormFileUrl() == null).collect(Collectors.toList()));
        //mgmt-org
        sbTotal.setMgmtOrgs(bPopsciMgmts.stream().filter(m -> m.getApplyType() == ORG).collect(Collectors.toList()));
        sbDraft.setMgmtOrgs(bPopsciMgmts.stream().filter(m -> m.getApplyType() == ORG && m.getStatus() == DRAFT).collect(Collectors.toList()));
        sbSubmit.setMgmtOrgs(bPopsciMgmts.stream().filter(m -> m.getApplyType() == ORG && m.getStatus() == SUBMIT).collect(Collectors.toList()));
        sbNoReccForm.setMgmtOrgs(bPopsciMgmts.stream().filter(m -> m.getApplyType() == ORG && m.getStatus() == SUBMIT && m.getReccFormFileUrl() == null).collect(Collectors.toList()));
        //works
        sbTotal.setWorks(bWorks);
        sbDraft.setWorks(bWorks.stream().filter(w -> w.getStatus() == DRAFT).collect(Collectors.toList()));
        sbSubmit.setWorks(bWorks.stream().filter(w -> w.getStatus() == SUBMIT).collect(Collectors.toList()));
        sbNoReccForm.setWorks(bWorks.stream().filter(w -> w.getStatus() == SUBMIT && w.getReccFormFileUrl() == null).collect(Collectors.toList()));
        //people
        sbTotal.setPeople(bPeople);
        sbDraft.setPeople(bPeople.stream().filter(p -> p.getStatus() == DRAFT).collect(Collectors.toList()));
        sbSubmit.setPeople(bPeople.stream().filter(p -> p.getStatus() == SUBMIT).collect(Collectors.toList()));
        sbNoReccForm.setPeople(bPeople.stream().filter(p -> p.getStatus() == SUBMIT && p.getReccFormFileUrl() == null).collect(Collectors.toList()));
        return new PageResult<>(Arrays.asList(sbTotal, sbDraft, sbSubmit, sbNoReccForm), 1, 4);
    }
}
