package org.example.catplatform.module.donation.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.catplatform.module.donation.entity.DonationCampaign;
import org.example.catplatform.module.donation.service.DonationCampaignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class DonationScheduledTask {

    @Autowired
    private DonationCampaignService campaignService;

    // 每天凌晨1点执行
    @Scheduled(cron = "0 0 1 * * ?")
    public void autoUpdateCampaignStatus() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<DonationCampaign> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DonationCampaign::getStatus, 1)   // 进行中
                .lt(DonationCampaign::getEndDate, now);
        List<DonationCampaign> campaigns = campaignService.list(wrapper);
        for (DonationCampaign campaign : campaigns) {
            campaign.setStatus(2); // 已结束
            campaignService.updateById(campaign);
        }
    }
}