// Same rule as the Android app and the server: the platform keeps 15%, the teacher gets the rest (85%).
export const PLATFORM_PERCENT = 15;
export const QUESTION_LIMIT = 3;

export const platformFee = (price: number) => Math.floor((price * PLATFORM_PERCENT) / 100);
export const teacherShare = (price: number) => price - platformFee(price);

/** 149000 -> "149 000" */
export const groupDigits = (n: number) => String(Math.round(n)).replace(/\B(?=(\d{3})+(?!\d))/g, " ");
