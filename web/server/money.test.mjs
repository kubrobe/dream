import { test } from "node:test";
import assert from "node:assert/strict";
import { split } from "./money.mjs";

test("85% to the teacher, 15% to Dream", () => {
  assert.deepEqual(split(100000), { price_uzs: 100000, platform_fee: 15000, teacher_share: 85000 });
});

test("the two parts always add up to the price", () => {
  for (const price of [0, 1, 7, 149000, 249999, 349000, 399001]) {
    const s = split(price);
    assert.equal(s.platform_fee + s.teacher_share, price);
    assert.ok(s.teacher_share >= s.platform_fee * 5);
  }
});

test("rounding favours the teacher", () => {
  assert.deepEqual(split(7), { price_uzs: 7, platform_fee: 1, teacher_share: 6 });
});

test("rejects broken prices", () => {
  assert.throws(() => split(-1));
  assert.throws(() => split(10.5));
});
