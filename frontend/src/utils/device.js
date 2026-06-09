export const isMobile = () => {
  if (typeof window === 'undefined') return false
  
  const userAgent = window.navigator.userAgent.toLowerCase()
  
  const mobileKeywords = [
    'iphone',
    'android',
    'phone',
    'mobile',
    'wap',
    'netfront',
    'java',
    'opera mobi',
    'opera mini',
    'ucweb',
    'windows ce',
    'symbian',
    'symbianos',
    'series60',
    'nokia',
    'blackberry',
    'webos',
    'sony ericsson',
    'bada',
    'windows phone',
    'mobile safari',
    'micromessenger',
    'wechat',
    'qq',
    'alipay',
    'dingtalk',
    'jdapp',
    'taobao'
  ]
  
  const tabletKeywords = [
    'ipad',
    'tablet',
    'playbook',
    'kindle',
    'nexus 7',
    'nexus 10',
    'galaxy tab',
    'xoom',
    'surface'
  ]
  
  for (const keyword of mobileKeywords) {
    if (userAgent.includes(keyword)) {
      return true
    }
  }
  
  for (const keyword of tabletKeywords) {
    if (userAgent.includes(keyword)) {
      return true
    }
  }
  
  return false
}

export const isTablet = () => {
  if (typeof window === 'undefined') return false
  
  const userAgent = window.navigator.userAgent.toLowerCase()
  
  const tabletKeywords = [
    'ipad',
    'tablet',
    'playbook',
    'kindle',
    'nexus 7',
    'nexus 10',
    'galaxy tab',
    'xoom',
    'surface'
  ]
  
  for (const keyword of tabletKeywords) {
    if (userAgent.includes(keyword)) {
      return true
    }
  }
  
  return false
}

export const detectDevice = () => {
  if (isTablet()) {
    return 'tablet'
  }
  if (isMobile()) {
    return 'mobile'
  }
  return 'desktop'
}
