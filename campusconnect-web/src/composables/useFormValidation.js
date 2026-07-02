/**
 * Form validation composable
 * Provides validation functions for authentication forms
 * 
 * @module useFormValidation
 * @description Comprehensive validation for login and registration forms,
 * including username, email, password, and captcha validation.
 * 
 * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.7, 1.8, 1.9, 1.10
 */

/**
 * List of commonly used weak passwords that should be rejected
 * regardless of their complexity score.
 * @constant {string[]}
 */
const WEAK_PASSWORDS = ['123456', '12345678', '123456789', 'password', 'admin', 'qwerty', '11111111', '88888888']

/**
 * Validation regex patterns for form fields
 * @constant {Object}
 */
const PATTERNS = {
  /** Username: 1-12 Chinese characters or alphanumeric combined (Logic handled in function) */
  username: /^[\u4e00-\u9fa5a-zA-Z0-9_-]{1,12}$/,
  /** Email: Strict standard email format */
  email: /^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/,
  /** Password: 8-16 chars, requires uppercase, lowercase, and digit */
  password: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[A-Za-z\d!@#$%^&*()_+\-={}\[\]:;"'<>,.?/]{8,16}$/
}

/**
 * Validates username (1-12 characters, Chinese/Alphanumeric/Underscore/Dash)
 * @param {string} value - Username to validate
 * @returns {ValidationResult}
 */
export function validateUsername(value) {
  if (!value || value.trim() === '') {
    return {
      isValid: false,
      errorField: 'username',
      errorMsg: '请输入用户名'
    }
  }

  if (value.length > 12) {
    return {
      isValid: false,
      errorField: 'username',
      errorMsg: '用户名不能超过12个字符'
    }
  }

  if (!PATTERNS.username.test(value)) {
    return {
      isValid: false,
      errorField: 'username',
      errorMsg: '用户名只能包含中文、字母、数字、下划线'
    }
  }

  return {
    isValid: true,
    errorField: '',
    errorMsg: ''
  }
}

/**
 * Validates email format strictly
 * @param {string} value - Email to validate
 * @returns {ValidationResult}
 */
export function validateEmail(value) {
  if (!value || value.trim() === '') {
    return {
      isValid: false,
      errorField: 'email',
      errorMsg: '请输入邮箱地址'
    }
  }

  if (!PATTERNS.email.test(value)) {
    return {
      isValid: false,
      errorField: 'email',
      errorMsg: '请输入有效的邮箱地址'
    }
  }

  return {
    isValid: true,
    errorField: '',
    errorMsg: ''
  }
}


/**
 * Validates password complexity (8-16 chars)
 * @param {string} value - Password to validate
 * @returns {ValidationResult}
 */
export function validatePassword(value) {
  if (!value || value.trim() === '') {
    return {
      isValid: false,
      errorField: 'password',
      errorMsg: '请输入密码'
    }
  }

  if (value.length < 8 || value.length > 16) {
    return {
      isValid: false,
      errorField: 'password',
      errorMsg: '密码长度必须为 8-16 位'
    }
  }

  // Check against weak password list first
  if (WEAK_PASSWORDS.includes(value.toLowerCase())) {
    return {
      isValid: false,
      errorField: 'password',
      errorMsg: '密码过于简单，请使用更复杂的密码'
    }
  }

  if (!PATTERNS.password.test(value)) {
    return {
      isValid: false,
      errorField: 'password',
      errorMsg: '密码必须包含大小写字母和数字'
    }
  }

  return {
    isValid: true,
    errorField: '',
    errorMsg: ''
  }
}

/**
 * Validates that confirm password matches password
 * @param {string} password - Original password
 * @param {string} confirmPassword - Confirmation password
 * @returns {ValidationResult}
 */
export function validateConfirmPassword(password, confirmPassword) {
  if (!confirmPassword || confirmPassword.trim() === '') {
    return {
      isValid: false,
      errorField: 'confirmPassword',
      errorMsg: '请确认密码'
    }
  }

  if (password !== confirmPassword) {
    return {
      isValid: false,
      errorField: 'confirmPassword',
      errorMsg: '两次输入的密码不一致'
    }
  }

  return {
    isValid: true,
    errorField: '',
    errorMsg: ''
  }
}

/**
 * Validates captcha input (case-insensitive)
 * @param {string} input - User's captcha input
 * @param {string} code - Generated captcha code
 * @returns {ValidationResult}
 */
export function validateCaptcha(input, code) {
  if (!input || input.trim() === '') {
    return {
      isValid: false,
      errorField: 'captcha',
      errorMsg: '请输入验证码'
    }
  }

  if (input.toUpperCase() !== code.toUpperCase()) {
    return {
      isValid: false,
      errorField: 'captcha',
      errorMsg: '验证码错误'
    }
  }

  return {
    isValid: true,
    errorField: '',
    errorMsg: ''
  }
}

/**
 * Validates login form (email + password)
 * @param {string} email - Email address
 * @param {string} password - Password
 * @returns {ValidationResult}
 */
export function validateLoginForm(email, password) {
  // Login allows username OR email, so we do a looser check if it doesn't look like an email
  if (!email || email.trim() === '') {
    return { isValid: false, errorField: 'login', errorMsg: '请输入账号' }
  }

  if (!password || password.trim() === '') {
    return { isValid: false, errorField: 'login', errorMsg: '请输入密码' }
  }

  // Allow login even if password format is slightly off (e.g. legacy accounts), 
  // but usually we would block obviously short passwords
  if (password.length < 6) {
    return { isValid: false, errorField: 'login', errorMsg: '密码长度错误' }
  }

  return {
    isValid: true,
    errorField: '',
    errorMsg: ''
  }
}

/**
 * Validates registration form (all fields)
 * @param {Object} formData - Form data object
 * @param {string} formData.username - Username
 * @param {string} formData.email - Email
 * @param {string} formData.password - Password
 * @param {string} formData.confirmPassword - Confirm password
 * @param {string} formData.captchaInput - Captcha input
 * @param {string} captchaCode - Generated captcha code
 * @returns {ValidationResult}
 */
export function validateRegisterForm(formData, captchaCode) {
  const usernameResult = validateUsername(formData.username)
  if (!usernameResult.isValid) {
    return usernameResult
  }

  const emailResult = validateEmail(formData.email)
  if (!emailResult.isValid) {
    return emailResult
  }

  const passwordResult = validatePassword(formData.password)
  if (!passwordResult.isValid) {
    return passwordResult
  }

  const confirmResult = validateConfirmPassword(formData.password, formData.confirmPassword)
  if (!confirmResult.isValid) {
    return confirmResult
  }

  const captchaResult = validateCaptcha(formData.captchaInput, captchaCode)
  if (!captchaResult.isValid) {
    return captchaResult
  }

  return {
    isValid: true,
    errorField: '',
    errorMsg: ''
  }
}

/**
 * Composable function that returns all validation functions
 * @returns {Object} Validation functions
 */
export function useFormValidation() {
  return {
    validateUsername,
    validateEmail,
    validatePassword,
    validateConfirmPassword,
    validateCaptcha,
    validateLoginForm,
    validateRegisterForm
  }
}
