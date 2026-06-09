import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/Login.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/register/Register.vue'),
        meta: { requiresAuth: false }
    },
    {
        path: '/forgot-password',
        name: 'ForgotPassword',
        component: () => import('@/views/login/ForgotPassword.vue'),
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
            { path: 'student/feedbacks', name: 'StudentFeedbacks', component: () => import('@/views/student/MyFeedbacks.vue') }
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
            { path: 'feedbacks', name: 'TeacherFeedbacks', component: () => import('@/views/student/MyFeedbacks.vue') }
        ]
    },
    {
        path: '/college',
        component: () => import('@/views/layout/CollegeLayout.vue'),
        meta: { requiresAuth: true, role: 'COLLEGE' },
        children: [
            { path: '', redirect: '/college/pending' },
            { path: 'pending', name: 'PendingAudit', component: () => import('@/views/college/AuditPending.vue') },
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
            { path: 'audit/pending', name: 'SchoolPendingAudit', component: () => import('@/views/school/AuditPending.vue') },
            { path: 'user/list', name: 'UserList', component: () => import('@/views/school/user/UserList.vue') },
            { path: 'feedback/list', name: 'AllFeedbacks', component: () => import('@/views/school/feedback/AllFeedbacks.vue') }
        ]
    },
    {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/common/Profile.vue'),
        meta: { requiresAuth: true }
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

router.beforeEach(async (to, from, next) => {
    const userStore = useUserStore()
    const requiresAuth = to.meta.requiresAuth !== false
    const requiredRole = to.meta.role

    if (requiresAuth && !userStore.isLoggedIn) {
        next('/login')
        return
    }

    if (requiredRole && userStore.role !== requiredRole) {
        const roleMap = {
            'STUDENT': '/',
            'TEACHER': '/teacher',
            'COLLEGE': '/college',
            'SCHOOL': '/school'
        }
        next(roleMap[userStore.role] || '/login')
        return
    }

    next()
})

export default router
