package org.example.catplatform.module.donation.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.catplatform.module.donation.entity.Donation;
import org.example.catplatform.module.donation.mapper.DonationMapper;
import org.example.catplatform.module.donation.service.DonationService;
import org.springframework.stereotype.Service;

@Service
public class DonationServiceImpl extends ServiceImpl<DonationMapper, Donation> implements DonationService {
}