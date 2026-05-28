package com.wyx.enrollment_promotion_systemmaster.controller;

import com.wyx.enrollment_promotion_systemmaster.dto.ApiResponse;
import com.wyx.enrollment_promotion_systemmaster.entity.Banner;
import com.wyx.enrollment_promotion_systemmaster.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/banners")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping
    public ApiResponse<List<Banner>> getActiveBanners() {
        List<Banner> banners = bannerService.getActiveBanners();
        return ApiResponse.success(banners);
    }

    @GetMapping("/public/list")
    public ApiResponse<List<Banner>> getPublicBanners() {
        List<Banner> banners = bannerService.getActiveBanners();
        return ApiResponse.success(banners);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ApiResponse<List<Banner>> getAllBanners() {
        List<Banner> banners = bannerService.list();
        return ApiResponse.success(banners);
    }

    @PostMapping
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ApiResponse<Banner> createBanner(@RequestBody Banner banner) {
        Banner created = bannerService.createBanner(banner);
        return ApiResponse.success(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ApiResponse<Banner> updateBanner(@PathVariable Long id, @RequestBody Banner banner) {
        Banner updated = bannerService.updateBanner(id, banner);
        return ApiResponse.success(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SCHOOL_ADMIN')")
    public ApiResponse<Void> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ApiResponse.success();
    }
}
