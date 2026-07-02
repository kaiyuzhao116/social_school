/**
 * Captcha composable
 * Provides captcha generation and validation for human verification
 * 
 * @module useCaptcha
 * @description Generates and validates 4-character alphanumeric captcha codes.
 * Uses a restricted character set to ensure readability.
 * 
 * Requirements: 8.1, 8.2, 8.3, 8.4
 */

import { ref } from 'vue'

/**
 * Valid characters for captcha generation.
 * Excludes easily confused characters: I, O (letters) and 0, 1 (digits)
 * to ensure the captcha is readable by humans.
 * @constant {string}
 */
const CAPTCHA_CHARS = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789'

/**
 * Length of generated captcha codes
 * @constant {number}
 */
const CAPTCHA_LENGTH = 4

/**
 * Generate a random captcha code
 * @returns {string} A 4-character captcha code
 */
export function generate() {
  let code = ''
  for (let i = 0; i < CAPTCHA_LENGTH; i++) {
    const randomIndex = Math.floor(Math.random() * CAPTCHA_CHARS.length)
    code += CAPTCHA_CHARS[randomIndex]
  }
  return code
}

/**
 * Validate user input against captcha code (case-insensitive)
 * @param {string} input - User's input
 * @param {string} code - The generated captcha code
 * @returns {boolean} True if input matches code (case-insensitive)
 */
export function validate(input, code) {
  if (!input || !code) {
    return false
  }
  return input.toUpperCase() === code.toUpperCase()
}

/**
 * Captcha composable for Vue components
 * Provides reactive captcha state and methods
 */
export function useCaptcha() {
  const captchaCode = ref('')
  const captchaInput = ref('')

  /**
   * Generate a new captcha code and update the reactive state
   * @returns {string} The newly generated captcha code
   */
  function generateCaptcha() {
    captchaCode.value = generate()
    return captchaCode.value
  }

  /**
   * Refresh the captcha by generating a new code and clearing input
   */
  function refresh() {
    captchaCode.value = generate()
    captchaInput.value = ''
  }

  /**
   * Validate the current input against the captcha code
   * @param {string} [input] - Optional input to validate (uses captchaInput.value if not provided)
   * @returns {boolean} True if validation passes
   */
  function validateCaptcha(input) {
    const inputToValidate = input !== undefined ? input : captchaInput.value
    return validate(inputToValidate, captchaCode.value)
  }

  // Initialize with a generated code
  captchaCode.value = generate()

  return {
    captchaCode,
    captchaInput,
    generate: generateCaptcha,
    refresh,
    validate: validateCaptcha
  }
}
