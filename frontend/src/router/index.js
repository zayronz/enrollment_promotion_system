import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { isMobile } from '@/utils/device'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/Login.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/forgot-password',
        name: 'ForgotPassword',
        component: () => import('@/views/login/ForgotPassword.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/identity/password-reset',
        name: 'IdentityPasswordResetMock',
        component: () => import('@/views/login/IdentityPasswordResetMock.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/register/Register.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/',
        component: () => import('@/views/layout/StudentLayout.vue'),
        meta: { requiresAuth: true, role: 'STUDENT' },
        children: [
            { path: '', redirect: '/student/activities' },
            { path: 'student/activities', name: 'StudentActivities', component: () => import('@/views/student/ActivityList.vue') },
            { path: 'student/activity/:id', name: 'StudentActivityDetail', component: () => import('@/views/student/ActivityDetail.vue') },
            { path: 'student/register/:id', name: 'RegistrationForm', component: () => import('@/views/student/RegistrationForm.vue') },
            { path: 'student/my-registrations', name: 'MyRegistrations', component: () => import('@/views/student/MyRegistrations.vue') },
            { path: 'student/feedbacks', name: 'StudentFeedbacks', component: () => import('@/views/student/MyFeedbacks.vue') },
            { path: 'student/test', name: 'StudentTestList', component: () => import('@/views/common/test/TestList.vue') },
            { path: 'student/test/start/:id', name: 'StudentTestStart', component: () => import('@/views/common/test/TestStart.vue') },
            { path: 'student/test/questions/:id', name: 'StudentTestQuestions', component: () => import('@/views/common/test/TestQuestions.vue') },
            { path: 'student/test/result/:id', name: 'StudentTestResult', component: () => import('@/views/common/test/TestResult.vue') },
            { path: 'student/my-activity', name: 'StudentMyActivity', component: () => import('@/views/h5/myActivity/MyActivity.vue') },
            { path: 'student/my-activity/:id', name: 'StudentMyActivityDetail', component: () => import('@/views/h5/myActivity/MyActivityDetail.vue') },
            { path: 'student/team', name: 'StudentTeam', component: () => import('@/views/h5/team/MyTeam.vue') },
            { path: 'student/materials', name: 'StudentMaterials', component: () => import('@/views/h5/materials/Materials.vue') }
        ]
    },
    {
        path: '/teacher',
        component: () => import('@/views/layout/TeacherLayout.vue'),
        meta: { requiresAuth: true, role: 'TEACHER' },
        children: [
            { path: '', redirect: '/teacher/activities' },
            { path: 'activities', name: 'TeacherActivities', component: () => import('@/views/teacher/ActivityList.vue') },
            { path: 'activity/:id', name: 'TeacherActivityDetail', component: () => import('@/views/teacher/ActivityDetail.vue') },
            { path: 'register/:id', name: 'TeacherRegistrationForm', component: () => import('@/views/teacher/RegistrationForm.vue') },
            { path: 'my-registrations', name: 'TeacherMyRegistrations', component: () => import('@/views/teacher/MyRegistrations.vue') },
            { path: 'feedbacks', name: 'TeacherFeedbacks', component: () => import('@/views/student/MyFeedbacks.vue') },
            { path: 'test', name: 'TeacherTestList', component: () => import('@/views/common/test/TestList.vue') },
            { path: 'test/start/:id', name: 'TeacherTestStart', component: () => import('@/views/common/test/TestStart.vue') },
            { path: 'test/questions/:id', name: 'TeacherTestQuestions', component: () => import('@/views/common/test/TestQuestions.vue') },
            { path: 'test/result/:id', name: 'TeacherTestResult', component: () => import('@/views/common/test/TestResult.vue') },
            { path: 'my-activity', name: 'TeacherMyActivity', component: () => import('@/views/h5/myActivity/MyActivity.vue') },
            { path: 'my-activity/:id', name: 'TeacherMyActivityDetail', component: () => import('@/views/h5/myActivity/MyActivityDetail.vue') },
            { path: 'team', name: 'TeacherTeam', component: () => import('@/views/h5/team/MyTeam.vue') },
            { path: 'approval', name: 'TeacherApprovalList', component: () => import('@/views/h5/approval/ApprovalList.vue') },
            { path: 'approval/:id', name: 'TeacherApprovalDetail', component: () => import('@/views/h5/approval/ApprovalDetail.vue') },
            { path: 'materials', name: 'TeacherMaterials', component: () => import('@/views/h5/materials/Materials.vue') }
        ]
    },
    {
        path: '/college',
        component: () => import('@/views/layout/CollegeLayout.vue'),
        meta: { requiresAuth: true, role: 'COLLEGE' },
        children: [
            { path: '', redirect: '/college/pending' },
            { path: 'pending', name: 'PendingAudit', component: () => import('@/views/college/PendingAudit.vue') },
            { path: 'history', name: 'AuditHistory', component: () => import('@/views/college/AuditHistory.vue') },
            { path: 'feedback', name: 'CollegeFeedback', component: () => import('@/views/college/FeedbackList.vue') },
            { path: 'statistics', name: 'CollegeStatistics', component: () => import('@/views/college/Statistics.vue') }
        ]
    },
    {
        path: '/school',
        component: () => import('@/views/layout/SchoolLayout.vue'),
        meta: { requiresAuth: true, role: 'SCHOOL' },
        children: [
            { path: '', redirect: '/school/dashboard' },
            { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/school/dashboard/Dashboard.vue') },
            { path: 'activity/list', name: 'ActivityList', component: () => import('@/views/school/activity/ActivityList.vue') },
            { path: 'activity/create', name: 'ActivityCreate', component: () => import('@/views/school/activity/ActivityCreate.vue') },
            { path: 'activity/edit/:id', name: 'ActivityEdit', component: () => import('@/views/school/activity/ActivityEdit.vue') },
            { path: 'activity/detail/:id', name: 'ActivityDetail', component: () => import('@/views/school/activity/ActivityDetail.vue') },
            { path: 'audit/pending', name: 'SchoolPendingAudit', component: () => import('@/views/school/audit/PendingAudit.vue') },
            { path: 'user/list', name: 'UserList', component: () => import('@/views/school/user/UserList.vue') },
            { path: 'feedback/list', name: 'AllFeedbacks', component: () => import('@/views/school/feedback/AllFeedbacks.vue') },
            { path: 'materials', name: 'SchoolMaterials', component: () => import('@/views/h5/materials/Materials.vue') }
        ]
    },
    {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/common/Profile.vue'),
        meta: { requiresAuth: true }
    },
    // ===== H5 移动端路由 =====
    {
        path: '/h5/login',
        name: 'H5Login',
        component: () => import('@/views/h5/login/H5Login.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/h5/forgot-password',
        name: 'H5ForgotPassword',
        component: () => import('@/views/login/ForgotPassword.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/h5/register',
        name: 'H5Register',
        component: () => import('@/views/register/Register.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/h5',
        component: () => import('@/views/h5/components/H5Layout.vue'),
        meta: { requiresAuth: true },
        children: [
            { path: '', redirect: '/h5/home' },
            { path: 'home', name: 'H5Home', component: () => import('@/views/h5/home/Home.vue') },
            { path: 'activity/:id', name: 'H5ActivityDetail', component: () => import('@/views/h5/activity/ActivityDetailReal.vue') },
            { path: 'school-activity/:id', name: 'H5SchoolActivityDetail', component: () => import('@/views/h5/activity/ActivityDetailReal.vue') },
            { path: 'register/:id', name: 'H5RegistrationForm', component: () => import('@/views/h5/activity/RegistrationForm.vue') },
            { path: 'submit-success', name: 'H5SubmitSuccess', component: () => import('@/views/h5/activity/SubmitSuccess.vue') },
            { path: 'more', name: 'H5More', component: () => import('@/views/h5/more/More.vue') },
            { path: 'test', name: 'H5TestList', component: () => import('@/views/h5/test/TestList.vue') },
            { path: 'test/start/:id', name: 'H5TestStart', component: () => import('@/views/h5/test/TestStart.vue') },
            { path: 'test/questions/:id', name: 'H5TestQuestions', component: () => import('@/views/h5/test/TestQuestions.vue') },
            { path: 'test/result/:id', name: 'H5TestResult', component: () => import('@/views/h5/test/TestResult.vue') },
            { path: 'my-activity', name: 'H5MyActivity', component: () => import('@/views/h5/myActivity/MyActivity.vue') },
            { path: 'my-activity/:id', name: 'H5MyActivityDetail', component: () => import('@/views/h5/myActivity/MyActivityDetail.vue') },
            { path: 'add-school', name: 'H5AddSchool', component: () => import('@/views/h5/myActivity/AddSchool.vue') },
            { path: 'add-video', name: 'H5AddVideo', component: () => import('@/views/h5/myActivity/AddVideo.vue') },
            { path: 'add-photo', name: 'H5AddPhoto', component: () => import('@/views/h5/myActivity/AddPhoto.vue') },
            { path: 'approval', name: 'H5ApprovalList', component: () => import('@/views/h5/approval/ApprovalList.vue') },
            { path: 'approval/:id', name: 'H5ApprovalDetail', component: () => import('@/views/h5/approval/ApprovalDetail.vue') },
            { path: 'team', name: 'H5MyTeam', component: () => import('@/views/h5/team/MyTeam.vue') },
            { path: 'materials', name: 'H5Materials', component: () => import('@/views/h5/materials/Materials.vue') },
            { path: 'my-registrations', name: 'H5MyRegistrations', component: () => import('@/views/h5/myActivity/MyRegistrations.vue') },
            { path: 'my-feedbacks', name: 'H5MyFeedbacks', component: () => import('@/views/h5/myActivity/MyFeedbacks.vue') },
            // 教师端 H5 路由
            { path: 'teacher/activities', name: 'H5TeacherActivities', component: () => import('@/views/h5/teacher/Activities.vue') },
            { path: 'teacher/activity/:id', name: 'H5TeacherActivityDetail', component: () => import('@/views/h5/teacher/ActivityDetail.vue') },
            { path: 'teacher/my-registrations', name: 'H5TeacherMyRegistrations', component: () => import('@/views/h5/teacher/MyRegistrations.vue') },
            // 学院端 H5 路由
            { path: 'college/pending', name: 'H5CollegePending', component: () => import('@/views/h5/college/PendingAudit.vue') },
            { path: 'college/history', name: 'H5CollegeHistory', component: () => import('@/views/h5/college/History.vue') },
            { path: 'college/feedback', name: 'H5CollegeFeedback', component: () => import('@/views/h5/college/Feedback.vue') },
            { path: 'college/feedback/:id', name: 'H5CollegeFeedbackDetail', component: () => import('@/views/h5/college/FeedbackDetail.vue') },
            // 学校端 H5 路由
            { path: 'school/dashboard', name: 'H5SchoolDashboard', component: () => import('@/views/h5/school/Dashboard.vue') },
            { path: 'school/activity-list', name: 'H5SchoolActivityList', component: () => import('@/views/h5/school/ActivityList.vue') },
            { path: 'school/activity-create', name: 'H5SchoolActivityCreate', component: () => import('@/views/h5/school/ActivityCreate.vue') },
            { path: 'school/activity-edit/:id', name: 'H5SchoolActivityEdit', component: () => import('@/views/h5/school/ActivityEdit.vue') },
            { path: 'school/audit-pending', name: 'H5SchoolAuditPending', component: () => import('@/views/h5/school/AuditPending.vue') },
            { path: 'school/user-list', name: 'H5SchoolUserList', component: () => import('@/views/h5/school/UserList.vue') },
            { path: 'school/feedback-list', name: 'H5SchoolFeedbackList', component: () => import('@/views/h5/school/FeedbackList.vue') },
            // 个人中心
            { path: 'profile', name: 'H5Profile', component: () => import('@/views/common/Profile.vue') },
            { path: 'change-password', name: 'H5ChangePassword', component: () => import('@/views/h5/profile/ChangePassword.vue') },
            { path: 'search-empty', name: 'H5SearchEmpty', component: () => import('@/views/h5/home/SearchEmpty.vue') },
            { path: 'network-error', name: 'H5NetworkError', component: () => import('@/views/h5/home/NetworkError.vue') },
            { path: 'no-content', name: 'H5NoContent', component: () => import('@/views/h5/home/NoContent.vue') }
        ]
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/common/NotFound.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

const roleHome = (role, mobile = false) => {
    if (mobile) {
        const h5Map = {
            STUDENT: '/h5/home',
            TEACHER: '/h5/teacher/activities',
            COLLEGE: '/h5/college/pending',
            SCHOOL: '/h5/school/dashboard'
        }
        return h5Map[role] || '/h5/home'
    }
    const pcMap = {
        STUDENT: '/student/activities',
        TEACHER: '/teacher/activities',
        COLLEGE: '/college/pending',
        SCHOOL: '/school/dashboard'
    }
    return pcMap[role] || '/login'
}

const toMobilePath = (path) => {
    const exactMap = {
        '/register': '/h5/register',
        '/forgot-password': '/h5/forgot-password',
        '/profile': '/h5/profile',
        '/student/activities': '/h5/home',
        '/student/my-registrations': '/h5/my-registrations',
        '/student/feedbacks': '/h5/my-feedbacks',
        '/student/test': '/h5/test',
        '/student/my-activity': '/h5/my-activity',
        '/student/team': '/h5/team',
        '/student/materials': '/h5/materials',
        '/teacher/activities': '/h5/teacher/activities',
        '/teacher/my-registrations': '/h5/teacher/my-registrations',
        '/teacher/feedbacks': '/h5/my-feedbacks',
        '/teacher/test': '/h5/test',
        '/teacher/team': '/h5/team',
        '/teacher/approval': '/h5/approval',
        '/teacher/materials': '/h5/materials',
        '/college/pending': '/h5/college/pending',
        '/college/history': '/h5/college/history',
        '/college/feedback': '/h5/college/feedback',
        '/school/dashboard': '/h5/school/dashboard',
        '/school/activity/list': '/h5/school/activity-list',
        '/school/activity/create': '/h5/school/activity-create',
        '/school/activity/edit': '/h5/school/activity-edit',
        '/school/audit/pending': '/h5/school/audit-pending',
        '/school/user/list': '/h5/school/user-list',
        '/school/feedback/list': '/h5/school/feedback-list',
        '/school/materials': '/h5/materials'
    }
    if (exactMap[path]) return exactMap[path]
    if (path.startsWith('/student/activity/')) return path.replace('/student/activity/', '/h5/activity/')
    if (path.startsWith('/teacher/activity/')) return path.replace('/teacher/activity/', '/h5/teacher/activity/')
    if (path.startsWith('/student/register/')) return path.replace('/student/register/', '/h5/register/')
    if (path.startsWith('/teacher/register/')) return path.replace('/teacher/register/', '/h5/register/')
    if (path.startsWith('/student/test/start/')) return path.replace('/student/test/start/', '/h5/test/start/')
    if (path.startsWith('/student/test/questions/')) return path.replace('/student/test/questions/', '/h5/test/questions/')
    if (path.startsWith('/student/test/result/')) return path.replace('/student/test/result/', '/h5/test/result/')
    if (path.startsWith('/teacher/test/start/')) return path.replace('/teacher/test/start/', '/h5/test/start/')
    if (path.startsWith('/teacher/test/questions/')) return path.replace('/teacher/test/questions/', '/h5/test/questions/')
    if (path.startsWith('/teacher/test/result/')) return path.replace('/teacher/test/result/', '/h5/test/result/')
    if (path.startsWith('/student/my-activity/')) return path.replace('/student/my-activity/', '/h5/my-activity/')
    if (path.startsWith('/teacher/my-activity/')) return path.replace('/teacher/my-activity/', '/h5/my-activity/')
    if (path.startsWith('/school/activity/edit/')) return path.replace('/school/activity/edit/', '/h5/school/activity-edit/')
    return null
}

// 路由守卫
router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore()
    const requiresAuth = to.meta.requiresAuth !== false
    const requiredRole = to.meta.role

    // 判断是否为 H5 页面
    const isH5Page = to.path.startsWith('/h5')

    // 移动端访问 PC 登录页时，自动进入 H5 登录页
    const deviceIsMobile = isMobile()
    if (to.path === '/login' && deviceIsMobile && !userStore.isLoggedIn) {
        next('/h5/login')
        return
    }

    if (requiresAuth && !userStore.isLoggedIn) {
        // 当前访问 H5 就回到 H5 登录页；PC 路由在移动端会回到 H5 登录页
        next(isH5Page ? '/h5/login' : '/login')
        return
    }

    // 移动端访问 PC 路由时，自动映射到对应 H5 路由
    if (userStore.isLoggedIn && deviceIsMobile && !isH5Page) {
        const mobilePath = toMobilePath(to.path)
        if (mobilePath) {
            next(mobilePath)
            return
        }
    }

    if (requiredRole && userStore.role !== requiredRole) {
        // 根据当前设备和访问端重定向到对应角色首页
        next(roleHome(userStore.role, deviceIsMobile || isH5Page))
        return
    }

    next()
})

export default router
