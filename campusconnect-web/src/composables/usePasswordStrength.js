/**
 * Password strength composable
 * Provides password strength calculation and weak password detection
 * 
 * @module usePasswordStrength
 * @description Evaluates password strength based on complexity criteria
 * and checks against a list of commonly used weak passwords.
 * 
 * Requirements: 1.5, 1.6, 7.3, 7.4
 */

/**
 * List of commonly used weak passwords that should be rejected
 * regardless of their complexity score.
 * Requirement 7.4: When a password matches a common weak password list, reject it
 * @constant {string[]}
 */
const WEAK_PASSWORDS = [
  '123456',
  '12345678',
  '123456789',
  'password',
  'admin',
  'qwerty'
]

/**
 * Checks if a password is in the weak password list
 * @param {string} password - Password to check
 * @returns {boolean} True if password is weak
 */
export function isWeakPassword(password) {
  if (!password) return false
  return WEAK_PASSWORDS.includes(password.toLowerCase())
}

/**
 * Calculates password strength score and returns strength information
 * 
 * Scoring criteria (Requirement 7.3):
 * - Length >= 8: +1 point
 * - Has uppercase letter: +1 point
 * - Has digit: +1 point
 * - Has special character: +1 point
 * 
 * Score mapping:
 * - 0-1: 弱 (Weak)
 * - 2: 中 (Medium)
 * - 3: 强 (Strong)
 * - 4: 极强 (Very Strong)
 * 
 * @param {string} password - Password to evaluate
 * @returns {PasswordStrength} Strength information object
 */
export function calculateStrength(password) {
  if (!password || password.length === 0) {
    return {
      score: 0,
      label: '弱',
      color: 'bg-red-500',
      textColor: 'text-red-500',
      percentage: 0
    }
  }

  let score = 0

  // Check length >= 8
  if (password.length >= 8) {
    score += 1
  }

  // Check for uppercase letter
  if (/[A-Z]/.test(password)) {
    score += 1
  }

  // Check for digit
  if (/\d/.test(password)) {
    score += 1
  }

  // Check for special character
  if (/[!@#$%^&*()_+\-={}\[\]:;"'<>,.?/]/.test(password)) {
    score += 1
  }

  // Map score to label and colors
  const strengthMap = {
    0: { label: '弱', color: 'bg-red-500', textColor: 'text-red-500' },
    1: { label: '弱', color: 'bg-red-500', textColor: 'text-red-500' },
    2: { label: '中', color: 'bg-yellow-500', textColor: 'text-yellow-500' },
    3: { label: '强', color: 'bg-green-500', textColor: 'text-green-500' },
    4: { label: '极强', color: 'bg-green-600', textColor: 'text-green-600' }
  }

  const { label, color, textColor } = strengthMap[score]

  return {
    score,
    label,
    color,
    textColor,
    percentage: (score / 4) * 100
  }
}

/**
 * Composable function that returns password strength functions
 * @returns {Object} Password strength functions
 */
export function usePasswordStrength() {
  return {
    calculateStrength,
    isWeakPassword
  }
}
