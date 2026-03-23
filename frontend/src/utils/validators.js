export const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
export const phoneRegex = /^\+?[\d\s-]{10,}$/;

export const validateEmail = (email) => {
  if (!email) return 'Email is required';
  if (!emailRegex.test(email)) return 'Invalid email format';
  return true;
};

export const validatePassword = (password) => {
  if (!password) return 'Password is required';
  if (password.length < 8) return 'Minimum 8 characters';
  if (!/[A-Z]/.test(password)) return 'Must include uppercase letter';
  if (!/[a-z]/.test(password)) return 'Must include lowercase letter';
  if (!/\d/.test(password)) return 'Must include a number';
  return true;
};

export const validatePrice = (price) => {
  if (!price) return 'Price is required';
  if (isNaN(price) || parseFloat(price) <= 0) return 'Must be greater than 0';
  return true;
};