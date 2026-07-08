import request from './request'

export function chatWithCampusAgent(data) {
    return request({
        url: '/agent/campus/chat',
        method: 'post',
        data
    })
}