<template>
  <div class="admin-knowledge-page">
    <div class="page-header">
      <div>
        <h1>知识库导入</h1>
        <p>把学校通知、办事流程、教务资料导入校园事务 Agent 知识库。</p>
      </div>

      <div class="header-badge">
        MySQL + Qdrant
      </div>
    </div>

    <div class="grid">
      <!-- 手动导入 -->
      <section class="card">
        <div class="card-header">
          <div>
            <h2>手动导入资料</h2>
            <p>适合导入缓考流程、成绩证明、奖学金材料、研究生事务等固定知识。</p>
          </div>
        </div>

        <div class="form">
          <label>
            标题
            <input
                v-model="manualForm.title"
                placeholder="例如：缓考申请流程"
            />
          </label>

          <div class="row">
            <label>
              来源名称
              <input
                  v-model="manualForm.sourceName"
                  placeholder="例如：渤海大学教务处"
              />
            </label>

            <label>
              来源类型
              <select v-model="manualForm.sourceType">
                <option value="教务处">教务处</option>
                <option value="研究生事务">研究生事务</option>
                <option value="就业网">就业网</option>
                <option value="学院通知">学院通知</option>
                <option value="学校官网">学校官网</option>
                <option value="手动补充">手动补充</option>
              </select>
            </label>
          </div>

          <div class="row">
            <label>
              可信度
              <select v-model="manualForm.trustLevel">
                <option value="高">高</option>
                <option value="中">中</option>
                <option value="低">低</option>
              </select>
            </label>

            <label>
              来源 URL
              <input
                  v-model="manualForm.url"
                  placeholder="没有真实链接也可以填 manual://defer-exam"
              />
            </label>
          </div>

          <label>
            正文内容
            <textarea
                v-model="manualForm.content"
                placeholder="把学校通知正文、办事流程、材料要求粘贴到这里..."
            ></textarea>
          </label>

          <button
              class="primary-btn"
              :disabled="manualLoading"
              @click="submitManualImport"
          >
            {{ manualLoading ? '导入中...' : '导入知识库' }}
          </button>
        </div>
      </section>

      <!-- 爬虫导入 -->
      <section class="card">
        <div class="card-header">
          <div>
            <h2>官网通知爬虫导入</h2>
            <p>适合批量导入学校官网通知公告，重复 URL 会自动跳过。</p>
          </div>
        </div>

        <div class="form">
          <label>
            列表接口 URL
            <input
                v-model="crawlForm.listUrl"
                placeholder="https://www.bhu.edu.cn/engine2/general/293065/type/more-datas"
            />
          </label>

          <div class="row">
            <label>
              来源名称
              <input v-model="crawlForm.sourceName" />
            </label>

            <label>
              来源类型
              <input v-model="crawlForm.sourceType" />
            </label>
          </div>

          <div class="row">
            <label>
              最大导入数量
              <input
                  v-model.number="crawlForm.maxCount"
                  type="number"
                  min="1"
                  max="20"
              />
            </label>

            <label>
              页码 pageNum
              <input
                  v-model.number="crawlForm.pageNum"
                  type="number"
                  min="1"
              />
            </label>
          </div>

          <div class="row">
            <label>
              engineInstanceId
              <input
                  v-model.number="crawlForm.engineInstanceId"
                  type="number"
              />
            </label>

            <label>
              typeId
              <input
                  v-model.number="crawlForm.typeId"
                  type="number"
              />
            </label>
          </div>

          <div class="row">
            <label>
              pageId
              <input
                  v-model.number="crawlForm.pageId"
                  type="number"
              />
            </label>

            <label>
              websiteId
              <input
                  v-model.number="crawlForm.websiteId"
                  type="number"
              />
            </label>
          </div>

          <button
              class="primary-btn"
              :disabled="crawlLoading"
              @click="submitCrawlerImport"
          >
            {{ crawlLoading ? '爬取中...' : '开始爬虫导入' }}
          </button>

          <button
              class="ghost-btn"
              :disabled="crawlLoading"
              @click="submitAutoImport"
          >
            一键导入渤大官网通知
          </button>
        </div>
      </section>
    </div>

    <!-- 结果展示 -->
    <section
        v-if="result"
        class="result-card"
    >
      <div class="result-header">
        <h2>导入结果</h2>
        <span
            class="status"
            :class="resultStatusClass"
        >
          {{ resultStatusText }}
        </span>
      </div>

      <pre>{{ prettyResult }}</pre>
    </section>

    <!-- 常用种子知识 -->
    <section class="card seed-card">
      <div class="card-header">
        <div>
          <h2>常用校园知识模板</h2>
          <p>点一下自动填充，然后你再按学校真实情况修改。</p>
        </div>
      </div>

      <div class="seed-list">
        <button
            v-for="seed in seeds"
            :key="seed.title"
            @click="fillSeed(seed)"
        >
          {{ seed.title }}
        </button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import {
  autoImportCampusKnowledge,
  crawlCampusKnowledge,
  importCampusKnowledge
} from '@/api/adminKnowledge'

const manualLoading = ref(false)
const crawlLoading = ref(false)
const result = ref(null)

const manualForm = ref({
  title: '',
  sourceName: '渤海大学',
  sourceType: '手动补充',
  trustLevel: '高',
  url: '',
  content: ''
})

const crawlForm = ref({
  listUrl: 'https://www.bhu.edu.cn/engine2/general/293065/type/more-datas',
  sourceName: '渤海大学官网',
  sourceType: '通知公告',
  trustLevel: '高',
  maxCount: 10,
  engineInstanceId: 425304,
  pageNum: 1,
  pageSize: 20,
  typeId: 2613739,
  sw: '',
  pageId: 55965,
  websiteId: 43901
})

const seeds = [
  {
    title: '缓考申请流程',
    sourceName: '渤海大学教务处',
    sourceType: '教务处',
    trustLevel: '中',
    url: 'manual://defer-exam',
    content: '学生因病或特殊原因不能按时参加考试时，可以申请缓考。一般需要填写缓考申请表，说明缓考原因，并提交相关证明材料。申请材料通常需要先交给所在学院审核，学院审核通过后，再按照教务处要求完成后续手续。学生应在学校规定时间内提交申请，逾期可能无法办理。具体材料和截止时间以学院或教务处最新通知为准。'
  },
  {
    title: '成绩证明办理流程',
    sourceName: '渤海大学教务处',
    sourceType: '教务处',
    trustLevel: '中',
    url: 'manual://grade-certificate',
    content: '学生如需办理成绩证明，一般需要联系学院教学秘书或学校教务处，确认成绩单打印、盖章和领取方式。办理时可能需要提供学生本人身份信息、学号、专业班级等信息。不同用途的成绩证明可能对盖章、份数和语言版本有不同要求，具体以教务处或学院通知为准。'
  },
  {
    title: '研究生材料提交提醒',
    sourceName: '渤海大学研究生事务',
    sourceType: '研究生事务',
    trustLevel: '中',
    url: 'manual://graduate-materials',
    content: '研究生在培养、评奖评优、学位申请、论文送审、答辩等环节，通常需要按学院或研究生院通知提交相关材料。材料可能包括申请表、成绩单、论文材料、导师意见、学院审核意见等。学生应重点关注研究生院和所在学院发布的通知，按要求在截止时间前完成提交。'
  },
  {
    title: '实习备案流程',
    sourceName: '渤海大学就业网',
    sourceType: '就业网',
    trustLevel: '中',
    url: 'manual://internship-record',
    content: '学生参加实习前，应关注学院或就业部门关于实习备案的要求。一般需要填写实习备案表，提交实习单位信息、实习时间、联系人、岗位内容等材料。部分学院可能还要求签订安全承诺书或实习协议。具体流程以学院和就业部门最新通知为准。'
  },
  {
    title: '奖学金申请材料',
    sourceName: '学院通知',
    sourceType: '学院通知',
    trustLevel: '中',
    url: 'manual://scholarship-apply',
    content: '学生申请奖学金时，一般需要根据学院通知准备申请表、成绩证明、获奖证明、科研成果、社会实践材料等。不同奖学金类型对成绩排名、综合测评、科研成果和思想表现要求不同。申请人应仔细阅读学院通知，按规定时间提交材料。'
  }
]

const prettyResult = computed(() => {
  return JSON.stringify(result.value, null, 2)
})

const resultStatusText = computed(() => {
  const data = normalizeData(result.value)

  if (!data) {
    return '无结果'
  }

  if (data.imported === false || data.successCount === 0) {
    return '已跳过重复'
  }

  return '导入成功'
})

const resultStatusClass = computed(() => {
  const data = normalizeData(result.value)

  if (data?.imported === false || data?.successCount === 0) {
    return 'status-skip'
  }

  return 'status-success'
})

const normalizeData = (res) => {
  if (res?.data?.data) {
    return res.data.data
  }

  if (res?.data) {
    return res.data
  }

  return res
}

const submitManualImport = async () => {
  if (!manualForm.value.title.trim()) {
    alert('请填写标题')
    return
  }

  if (!manualForm.value.url.trim()) {
    alert('请填写来源 URL，没有真实链接可以填 manual://xxx')
    return
  }

  if (!manualForm.value.content.trim()) {
    alert('请填写正文内容')
    return
  }

  manualLoading.value = true

  try {
    const res = await importCampusKnowledge(manualForm.value)
    result.value = normalizeData(res)
    alert('导入完成')
  } catch (e) {
    console.error(e)
    alert('导入失败，请看控制台或后端日志')
  } finally {
    manualLoading.value = false
  }
}

const submitCrawlerImport = async () => {
  crawlLoading.value = true

  try {
    const res = await crawlCampusKnowledge(crawlForm.value)
    result.value = normalizeData(res)
    alert('爬虫导入完成')
  } catch (e) {
    console.error(e)
    alert('爬虫导入失败，请看控制台或后端日志')
  } finally {
    crawlLoading.value = false
  }
}

const submitAutoImport = async () => {
  crawlLoading.value = true

  try {
    const res = await autoImportCampusKnowledge()
    result.value = normalizeData(res)
    alert('自动导入完成')
  } catch (e) {
    console.error(e)
    alert('自动导入接口不可用，请确认后端是否加了 /crawler/auto-test')
  } finally {
    crawlLoading.value = false
  }
}

const fillSeed = (seed) => {
  manualForm.value = {
    ...seed
  }

  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}
</script>

<style scoped>
.admin-knowledge-page {
  padding: 40px;
  min-height: 100vh;
  background: #f8fafc;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  align-items: flex-start;
  margin-bottom: 28px;
}

.page-header h1 {
  font-size: 32px;
  font-weight: 900;
  color: #172033;
  margin: 0 0 8px;
}

.page-header p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.header-badge {
  padding: 8px 14px;
  border-radius: 999px;
  background: #dcfce7;
  color: #166534;
  font-size: 13px;
  font-weight: 800;
}

.grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 22px;
}

.card,
.result-card {
  background: rgba(255, 255, 255, 0.88);
  border: 1px solid #e5e7eb;
  border-radius: 26px;
  padding: 24px;
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.06);
}

.card-header {
  margin-bottom: 20px;
}

.card-header h2,
.result-header h2 {
  font-size: 18px;
  font-weight: 900;
  color: #172033;
  margin: 0 0 6px;
}

.card-header p {
  font-size: 13px;
  color: #64748b;
  margin: 0;
  line-height: 1.7;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 7px;
  font-size: 13px;
  font-weight: 700;
  color: #334155;
}

input,
select,
textarea {
  width: 100%;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 12px 14px;
  background: #f8fafc;
  outline: none;
  color: #172033;
  font-size: 14px;
}

textarea {
  min-height: 220px;
  resize: vertical;
  line-height: 1.7;
}

input:focus,
select:focus,
textarea:focus {
  border-color: #4f7d50;
  background: #ffffff;
  box-shadow: 0 0 0 3px rgba(79, 125, 80, 0.12);
}

.primary-btn,
.ghost-btn {
  height: 46px;
  border-radius: 15px;
  border: none;
  font-weight: 900;
  cursor: pointer;
  transition: 0.2s;
}

.primary-btn {
  background: #4f7d50;
  color: white;
}

.primary-btn:hover {
  background: #426a43;
}

.ghost-btn {
  background: #f1f5f9;
  color: #334155;
  border: 1px solid #e5e7eb;
}

.ghost-btn:hover {
  background: #e2e8f0;
}

button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.result-card {
  margin-top: 22px;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status {
  padding: 5px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
}

.status-success {
  background: #dcfce7;
  color: #166534;
}

.status-skip {
  background: #fef3c7;
  color: #92400e;
}

pre {
  margin-top: 16px;
  padding: 16px;
  border-radius: 16px;
  background: #0f172a;
  color: #d1fae5;
  overflow: auto;
  font-size: 13px;
  line-height: 1.6;
}

.seed-card {
  margin-top: 22px;
}

.seed-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.seed-list button {
  padding: 10px 14px;
  border-radius: 999px;
  border: 1px solid #dbeafe;
  background: #eff6ff;
  color: #1d4ed8;
  font-size: 13px;
  font-weight: 800;
  cursor: pointer;
}

.seed-list button:hover {
  background: #dbeafe;
}

@media (max-width: 1100px) {
  .grid {
    grid-template-columns: 1fr;
  }

  .admin-knowledge-page {
    padding: 24px;
  }
}
</style>