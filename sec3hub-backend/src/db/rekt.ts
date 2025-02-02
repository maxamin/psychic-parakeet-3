import { sql } from "./db.ts";

// custom sort logic
const getRekts = async (
  page: number,
  limit: number,
  sort: string,
  keyword: string,
  order: string
) => {
  // get the offset from the page and limit
  const offset = (page - 1) * limit;

  const isDesc = order === "desc" ? true : false;

  let rekts;

  if (!keyword) {
    rekts = await sql`
        SELECT * FROM rekt
        ${
          isDesc
            ? sql`ORDER BY ${sql(sort)} DESC`
            : sql`ORDER BY ${sql(sort)} ASC`
        }
        OFFSET ${offset}
        LIMIT ${limit}
        `;
  } else {
    rekts = await sql`
        SELECT * FROM rekt 
        WHERE name ILIKE ${"%" + keyword + "%"}
        OR category ILIKE ${"%" + keyword + "%"}
        or chain ILIKE ${"%" + keyword + "%"}
        OR audited_by::text ILIKE ${"%" + keyword + "%"}
        OR txs::text ILIKE ${"%" + keyword + "%"}
        or sources::text ILIKE ${"%" + keyword + "%"}

        ${
          isDesc
            ? sql`ORDER BY ${sql(sort)} DESC`
            : sql`ORDER BY ${sql(sort)} ASC`
        }
        OFFSET ${offset}
        LIMIT ${limit}
        `;
  }

  return rekts;
};

// get total number of rekts
const getTotalRekts = async () => {
  const totalRekts = await sql`
    SELECT COUNT(*) FROM rekt`;
  return totalRekts;
};

// get some of damage
const getTotalDamage = async () => {
  const totalDamage = await sql`
    SELECT SUM(damage) FROM rekt`;
  return totalDamage;
};

// get last rekt
const lastAddedRekt = async () => {
  const lastAddedRekt = await sql`
    SELECT * FROM rekt ORDER BY id DESC LIMIT 1`;
  return lastAddedRekt;
};

// get total keyword hits
const getTotalKeywordHits = async (keyword: string) => {
  const totalKeywordHits = await sql`
    SELECT COUNT(*) FROM rekt 
    WHERE name ILIKE ${"%" + keyword + "%"}
    OR category ILIKE ${"%" + keyword + "%"}
    or chain ILIKE ${"%" + keyword + "%"}
    OR audited_by::text ILIKE ${"%" + keyword + "%"}
    OR txs::text ILIKE ${"%" + keyword + "%"}
    or sources::text ILIKE ${"%" + keyword + "%"}
      `;
  return totalKeywordHits;
};

// stats
const getStats = async () => {
  const totalRekts = await getTotalRekts();
  const totalDamage = await getTotalDamage();
  const lastRecord = await lastAddedRekt();
  const stats = {
    totalRekts,
    totalDamage,
    lastRecord,
  };
  return stats;
};

export const RektsService = {
  getRekts,
  getTotalRekts,
  getTotalKeywordHits,
  getStats,
};
