package com.wyx.enrollment_promotion_systemmaster.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wyx.enrollment_promotion_systemmaster.entity.Banner;
import com.wyx.enrollment_promotion_systemmaster.exception.BusinessException;
import com.wyx.enrollment_promotion_systemmaster.mapper.BannerMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BannerService extends ServiceImpl<BannerMapper, Banner> {

    public List<Banner> getActiveBanners() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getStatus, 1)
                .orderByAsc(Banner::getSort);
        return this.list(wrapper);
    }

    public Banner createBanner(Banner banner) {
        this.save(banner);
        return banner;
    }

    public Banner updateBanner(Long id, Banner banner) {
        Banner existingBanner = this.getById(id);
        if (existingBanner == null) {
            throw new BusinessException("Banner not found");
        }

        existingBanner.setTitle(banner.getTitle());
        existingBanner.setImageUrl(banner.getImageUrl());
        existingBanner.setLinkUrl(banner.getLinkUrl());
        existingBanner.setActivityId(banner.getActivityId());
        existingBanner.setSort(banner.getSort());
        existingBanner.setStatus(banner.getStatus());

        this.updateById(existingBanner);
        return existingBanner;
    }

    public void deleteBanner(Long id) {
        if (!this.removeById(id)) {
            throw new BusinessException("Banner not found");
        }
    }
}
