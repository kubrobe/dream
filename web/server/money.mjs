// The one money rule of Dream, used by the API: the platform keeps 15%, the teacher gets the rest (85%).
// Same rule as the Android app (DreamRepository.platformFee) and the website (src/lib/money.ts).
export const PLATFORM_PERCENT = 15;

export function split(priceUzs) {
  if (!Number.isInteger(priceUzs) || priceUzs < 0) throw new RangeError("price must be a whole number of UZS, 0 or more");
  const platform_fee = Math.floor((priceUzs * PLATFORM_PERCENT) / 100);
  return { price_uzs: priceUzs, platform_fee, teacher_share: priceUzs - platform_fee };
}
