<template>
  <div class="banner-carousel" v-if="banners.length > 0">
    <t-carousel
      :autoplay="autoplay"
      :interval="interval"
      :loop="loop"
      :direction="direction"
      :height="height"
      @change="handleChange"
    >
      <t-carousel-item
        v-for="banner in banners"
        :key="banner.id"
        :style="{ cursor: banner.linkUrl ? 'pointer' : 'default' }"
        @click="handleClick(banner)"
      >
        <t-image
          :src="banner.fullImageUrl"
          :style="{ width: '100%', height: '100%', objectFit: 'cover' }"
          fit="cover"
          :lazy="true"
          :overlay-trigger="'hover'"
          :overlay-content="banner.activityName ? `查看详情: ${banner.activityName}` : ''"
        />
      </t-carousel-item>
    </t-carousel>
    
    <!-- 自定义指示器 -->
    <div class="carousel-indicators" v-if="showIndicators">
      <span
        v-for="(banner, index) in banners"
        :key="banner.id"
        :class="['indicator', { active: currentIndex === index }]"
        @click="goTo(index)"
      />
    </div>
  </div>
  
  <!-- 无轮播图时的占位 -->
  <div v-else class="banner-placeholder" :style="{ height: height }">
    <t-empty description="暂无轮播图" />
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { getActiveBanners } from '@/api/banner'
import { useRouter } from 'vue-router'

const props = defineProps({
  // 是否自动播放
  autoplay: {
    type: Boolean,
    default: true
  },
  // 自动播放间隔（毫秒）
  interval: {
    type: Number,
    default: 5000
  },
  // 是否循环播放
  loop: {
    type: Boolean,
    default: true
  },
  // 轮播方向
  direction: {
    type: String,
    default: 'horizontal' // horizontal | vertical
  },
  // 轮播图高度
  height: {
    type: String,
    default: '400px'
  },
  // 是否显示指示器
  showIndicators: {
    type: Boolean,
    default: true
  },
  // 指定活动ID，只显示该活动的轮播图
  activityId: {
    type: [Number, String],
    default: null
  }
})

const emit = defineEmits(['change', 'click'])

const router = useRouter()
const banners = ref([])
const currentIndex = ref(0)

const loadBanners = async () => {
  try {
    const res = await getActiveBanners()
    if (res.code === 200) {
      // 如果指定了活动ID，过滤出该活动的轮播图
      if (props.activityId) {
        banners.value = res.data.filter(b => b.activityId == props.activityId)
      } else {
        banners.value = res.data
      }
    }
  } catch (error) {
    console.error('加载轮播图失败:', error)
  }
}

const handleChange = (index) => {
  currentIndex.value = index
  emit('change', index)
}

const handleClick = (banner) => {
  emit('click', banner)
  
  // 如果有跳转链接，跳转到对应页面
  if (banner.linkUrl) {
    if (banner.linkUrl.startsWith('http://') || banner.linkUrl.startsWith('https://')) {
      window.location.href = banner.linkUrl
    } else {
      router.push(banner.linkUrl)
    }
  } else if (banner.activityId) {
    // 如果没有链接但有关联活动，跳转到活动详情
    router.push(`/activity/detail/${banner.activityId}`)
  }
}

const goTo = (index) => {
  currentIndex.value = index
  emit('change', index)
}

// 监听活动ID变化
watch(() => props.activityId, () => {
  loadBanners()
})

// 页面加载
onMounted(() => {
  loadBanners()
})

// 暴露方法
defineExpose({
  reload: loadBanners
})
</script>

<style scoped>
.banner-carousel {
  position: relative;
  width: 100%;
}

.carousel-indicators {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 8px;
  z-index: 10;
}

.indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  transition: all 0.3s ease;
}

.indicator:hover {
  background-color: rgba(255, 255, 255, 0.8);
}

.indicator.active {
  background-color: #fff;
  transform: scale(1.2);
}

.banner-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
  border-radius: 8px;
}
</style>
