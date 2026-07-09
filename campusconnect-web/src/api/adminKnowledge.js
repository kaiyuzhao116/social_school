import request from '@/api/request'

// 手动导入校园知识
export function importCampusKnowledge(data) {
    return request.post('/agent/campus/knowledge/import', data)
}

// 爬虫导入校园通知
export function crawlCampusKnowledge(data) {
    return request.post('/agent/campus/crawler/import', data)
}

// 自动爬虫测试接口，如果你后端加了 /crawler/auto-test 就能用
export function autoImportCampusKnowledge() {
    return request.post('/agent/campus/crawler/auto-test')
}