import request from '@/utils/request'

export const fileApi = {
    // 上传文件
    upload(file, relatedId, relatedType) {
        const formData = new FormData()
        formData.append('file', file)
        if (relatedId) formData.append('relatedId', relatedId)
        if (relatedType) formData.append('relatedType', relatedType)

        return request.post('/file/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        })
    }
}