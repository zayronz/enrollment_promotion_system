package com.edu.enrollment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.enrollment.dto.BannerDTO;
import com.edu.enrollment.service.BannerService;
import com.edu.enrollment.vo.BannerVO;
import com.edu.enrollment.vo.ResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 轮播图控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/banner")
@RequiredArgsConstructor
public class BannerController {
    
    private final BannerService bannerService;
    
    /**
     * 创建轮播图
     */
    @PostMapping
    public ResultVO<BannerVO> create(@RequestBody BannerDTO dto) {
        try {
            log.info("【轮播图控制器】创建轮播图: {}", dto);
            BannerVO banner = bannerService.create(dto);
            return ResultVO.success(banner);
        } catch (Exception e) {
            log.error("【轮播图控制器】创建轮播图失败: {}", e.getMessage(), e);
            return ResultVO.error("创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新轮播图
     */
    @PutMapping("/{id}")
    public ResultVO<BannerVO> update(@PathVariable Long id, @RequestBody BannerDTO dto) {
        try {
            log.info("【轮播图控制器】更新轮播图: id={}", id);
            BannerVO banner = bannerService.update(id, dto);
            return ResultVO.success(banner);
        } catch (Exception e) {
            log.error("【轮播图控制器】更新轮播图失败: {}", e.getMessage(), e);
            return ResultVO.error("更新失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除轮播图
     */
    @DeleteMapping("/{id}")
    public ResultVO<Void> delete(@PathVariable Long id) {
        try {
            log.info("【轮播图控制器】删除轮播图: id={}", id);
            bannerService.delete(id);
            return ResultVO.success();
        } catch (Exception e) {
            log.error("【轮播图控制器】删除轮播图失败: {}", e.getMessage(), e);
            return ResultVO.error("删除失败: " + e.getMessage());
        }
    }
    
    /**
     * 查询轮播图详情
     */
    @GetMapping("/{id}")
    public ResultVO<BannerVO> getById(@PathVariable Long id) {
        try {
            log.info("【轮播图控制器】查询轮播图详情: id={}", id);
            BannerVO banner = bannerService.getById(id);
            return ResultVO.success(banner);
        } catch (Exception e) {
            log.error("【轮播图控制器】查询轮播图详情失败: {}", e.getMessage(), e);
            return ResultVO.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 分页查询轮播图列表
     */
    @GetMapping("/page")
    public ResultVO<?> listPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long activityId,
            @RequestParam(required = false) Integer status) {
        try {
            log.info("【轮播图控制器】分页查询轮播图: pageNum={}, pageSize={}", pageNum, pageSize);
            Page<BannerVO> page = bannerService.listPage(pageNum, pageSize, activityId, status);
            
            // 使用 Map 返回分页结果，避免 Page 对象序列化问题
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("records", page.getRecords());
            result.put("total", page.getTotal());
            result.put("current", page.getCurrent());
            result.put("pageSize", page.getSize());
            result.put("pages", page.getPages());
            
            return ResultVO.success(result);
        } catch (Exception e) {
            log.error("【轮播图控制器】分页查询轮播图失败: {}", e.getMessage(), e);
            return ResultVO.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有启用的轮播图（用于前端展示）
     */
    @GetMapping("/active")
    public ResultVO<List<BannerVO>> getActiveBanners() {
        try {
            log.info("【轮播图控制器】获取所有启用的轮播图");
            List<BannerVO> banners = bannerService.getActiveBanners();
            return ResultVO.success(banners);
        } catch (Exception e) {
            log.error("【轮播图控制器】获取轮播图失败: {}", e.getMessage(), e);
            return ResultVO.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据活动ID获取轮播图列表
     */
    @GetMapping("/activity/{activityId}")
    public ResultVO<List<BannerVO>> getByActivityId(@PathVariable Long activityId) {
        try {
            log.info("【轮播图控制器】根据活动ID获取轮播图: activityId={}", activityId);
            List<BannerVO> banners = bannerService.getByActivityId(activityId);
            return ResultVO.success(banners);
        } catch (Exception e) {
            log.error("【轮播图控制器】获取轮播图失败: {}", e.getMessage(), e);
            return ResultVO.error("查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量创建轮播图
     */
    @PostMapping("/batch")
    public ResultVO<List<BannerVO>> batchCreate(@RequestBody List<BannerDTO> dtoList) {
        try {
            log.info("【轮播图控制器】批量创建轮播图: count={}", dtoList.size());
            List<BannerVO> banners = bannerService.batchCreate(dtoList);
            return ResultVO.success(banners);
        } catch (Exception e) {
            log.error("【轮播图控制器】批量创建轮播图失败: {}", e.getMessage(), e);
            return ResultVO.error("批量创建失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新轮播图状态
     */
    @PatchMapping("/{id}/status")
    public ResultVO<BannerVO> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        try {
            log.info("【轮播图控制器】更新轮播图状态: id={}, status={}", id, status);
            BannerVO banner = bannerService.updateStatus(id, status);
            return ResultVO.success(banner);
        } catch (Exception e) {
            log.error("【轮播图控制器】更新轮播图状态失败: {}", e.getMessage(), e);
            return ResultVO.error("更新状态失败: " + e.getMessage());
        }
    }
}
