import { sql } from "./db.ts";

const getAuditors = async (
  page: number,
  limit: number,
  sort: string,
  keyword: string,
  order: string
) => {
  // get the offset from the page and limit
  const offset = (page - 1) * limit;

  // stuff we want to sort descending the rest will be ascending
  // const descSort = ["total_audits", "up_votes", "start_year", "created_at"];
  // const isDesc = descSort.includes(sort) ? true : false;

  const isDesc = order === "desc" ? true : false;

  let auditors;

  if (!keyword) {
    auditors = await sql`
        SELECT * FROM auditor
        ${
          isDesc
            ? sql`ORDER BY ${sql(sort)} DESC`
            : sql`ORDER BY ${sql(sort)} ASC`
        }
        OFFSET ${offset}
        LIMIT ${limit}
        `;
  } else {
    auditors = await sql`
        SELECT * FROM auditor 
        WHERE name ILIKE  ${"%" + keyword + "%"}
        OR type ILIKE ${"%" + keyword + "%"}
        OR location ILIKE ${"%" + keyword + "%"}
        OR services::text ILIKE ${"%" + keyword + "%"}

        ${
          isDesc
            ? sql`ORDER BY ${sql(sort)} DESC`
            : sql`ORDER BY ${sql(sort)} ASC`
        }
        OFFSET ${offset}
        LIMIT ${limit}
        `;
  }

  return auditors;
};

// get total keyword hits
const getTotalKeywordHits = async (keyword: string) => {
  const totalKeywordHits = await sql`
    SELECT COUNT(*) FROM auditor
    WHERE name ILIKE ${"%" + keyword + "%"}
    OR type ILIKE ${"%" + keyword + "%"}
    OR location ILIKE ${"%" + keyword + "%"}
    OR services::text ILIKE ${"%" + keyword + "%"}
    `;
  return totalKeywordHits;
};

// get total number of auditors
const getTotalAuditors = async () => {
  const totalAuditors = await sql`
    SELECT COUNT(*) FROM auditor`;
  return totalAuditors;
};

// gets sum of total_audits for all auditors
const getTotalAudits = async () => {
  const totalAudits = await sql`
    SELECT SUM(total_audits) FROM auditor`;
  return totalAudits;
};

const lastAddedAuditor = async () => {
  const lastAddedAuditor = await sql`
    SELECT * FROM auditor
    ORDER BY id DESC
    LIMIT 1`;
  return lastAddedAuditor;
};

const getStats = async () => {
  const totalAuditors = await getTotalAuditors();
  const totalAudits = await getTotalAudits();
  const lastRecord = await lastAddedAuditor();

  return {
    totalAuditors,
    totalAudits,
    lastRecord,
  };
};

// to make it work with sinons stub we need to wrap the function in an object
export const AuditorsService = {
  getAuditors,
  getTotalAuditors,
  getTotalKeywordHits,
  getStats,
};
