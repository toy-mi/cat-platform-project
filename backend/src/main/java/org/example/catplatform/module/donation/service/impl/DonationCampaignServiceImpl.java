package org.example.catplatform.module.donation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.catplatform.module.donation.entity.DonationCampaign;
import org.example.catplatform.module.donation.mapper.DonationCampaignMapper;
import org.example.catplatform.module.donation.service.DonationCampaignService;
import org.springframework.stereotype.Service;

@Service
public class DonationCampaignServiceImpl extends ServiceImpl<DonationCampaignMapper, DonationCampaign> implements DonationCampaignService {
}