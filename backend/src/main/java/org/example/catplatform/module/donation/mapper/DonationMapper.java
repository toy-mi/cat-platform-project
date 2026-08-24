package org.example.catplatform.module.donation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.catplatform.module.donation.entity.Donation;

@Mapper
public interface DonationMapper extends BaseMapper<Donation> {
}