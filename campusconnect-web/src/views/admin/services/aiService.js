// AI内容审核服务 - 使用SiliconFlow API

const getConfig = () => ({
  apiUrl: import.meta.env.VITE_AI_API_URL || 'https://api.siliconflow.cn/v1/chat/completions',
  apiKey: import.meta.env.VITE_SILICON_API_KEY,
  model: import.meta.env.VITE_AI_MODEL || 'THUDM/GLM-4.1V-9B-Thinking'
})

// 内容安全审核
export const analyzeContentSafety = async (text) => {
  const { apiUrl, apiKey, model } = getConfig()
  
  if (!apiKey) {
    console.warn('SiliconFlow API Key not configured, using fallback detection')
    return fallbackDetection(text)
  }

  try {
    const response = await fetch(apiUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${apiKey}`
      },
      body: JSON.stringify({
        model,
        messages: [
          {
            role: 'system',
            content: `你是一个专业的内容安全审核员，负责审核校园社交平台的用户发布内容。
请分析以下内容是否违反社区规范，需要检测以下类型的违规内容：
1. 辱骂、攻击性言论
2. 广告、推销信息
3. 违规服务（如代课、代考、作弊）
4. 色情、暴力内容
5. 虚假信息、谣言

请以JSON格式返回审核结果：
{
  "safe": true/false,
  "reason": "审核结论说明",
  "confidence": 0-100的置信度数值
}`
          },
          {
            role: 'user',
            content: `请审核以下内容：\n\n${text}`
          }
        ],
        temperature: 0.3,
        max_tokens: 200
      })
    })

    if (!response.ok) {
      throw new Error(`API request failed: ${response.status}`)
    }

    const data = await response.json()
    const content = data?.choices?.[0]?.message?.content || ''
    
    // 解析AI返回的JSON
    try {
      const jsonMatch = content.match(/\{[\s\S]*\}/)
      if (jsonMatch) {
        const result = JSON.parse(jsonMatch[0])
        return {
          safe: Boolean(result.safe),
          reason: result.reason || '审核完成',
          confidence: Math.min(100, Math.max(0, Number(result.confidence) || 80))
        }
      }
    } catch (parseError) {
      console.warn('Failed to parse AI response, using content analysis')
    }

    // 如果无法解析JSON，根据内容判断
    const isSafe = !content.includes('不安全') && !content.includes('违规') && !content.includes('false')
    return {
      safe: isSafe,
      reason: content.slice(0, 100),
      confidence: 75
    }
  } catch (error) {
    console.error('SiliconFlow API Error:', error)
    return fallbackDetection(text)
  }
}

// 备用检测（当API不可用时）
const fallbackDetection = (text) => {
  const unsafeKeywords = ['垃圾', '骂', '代课', '作弊', '枪手', '代写', '包过', '死', '滚']
  const hasUnsafeContent = unsafeKeywords.some(keyword => text.includes(keyword))
  
  return {
    safe: !hasUnsafeContent,
    reason: hasUnsafeContent ? '检测到疑似违规关键词（本地检测）' : '内容符合社区规范（本地检测）',
    confidence: hasUnsafeContent ? 70 : 60
  }
}

// 批量内容审核
export const batchAnalyzeContent = async (texts) => {
  const results = await Promise.all(
    texts.map(text => analyzeContentSafety(text))
  )
  return results
}

// AI辅助生成审核意见
export const generateModerationSuggestion = async (content, context = '') => {
  const { apiUrl, apiKey, model } = getConfig()
  
  if (!apiKey) {
    return '请人工审核此内容'
  }

  try {
    const response = await fetch(apiUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${apiKey}`
      },
      body: JSON.stringify({
        model,
        messages: [
          {
            role: 'system',
            content: '你是校园社交平台的内容审核助手，请为管理员提供专业的审核建议。'
          },
          {
            role: 'user',
            content: `请为以下内容提供审核建议：\n\n内容：${content}\n${context ? `\n背景信息：${context}` : ''}\n\n请简洁回答，50字以内。`
          }
        ],
        temperature: 0.5,
        max_tokens: 100
      })
    })

    const data = await response.json()
    return data?.choices?.[0]?.message?.content || '建议人工审核'
  } catch (error) {
    console.error('Generate suggestion error:', error)
    return '建议人工审核'
  }
}
