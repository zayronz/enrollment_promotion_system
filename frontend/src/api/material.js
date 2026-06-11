import request from '@/utils/request'

export const materialApi = {
    list(params) {
        return request.get('/material/list', { params })
    },

    upload(data) {
        const formData = new FormData()
        formData.append('file', data.file)
        formData.append('name', data.name)
        formData.append('category', data.category)
        if (data.description) {
            formData.append('description', data.description)
        }

        return request.post('/material/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        })
    },

    delete(id) {
        return request.delete(`/material/${id}`)
    }
}
