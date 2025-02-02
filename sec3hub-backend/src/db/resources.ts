import { sql } from "./db.ts";

// custom sort logic
const getResources = async (
  page: number,
  limit: number,
  sort: string,
  keyword: string,
  order: string
) => {
  // get the offset from the page and limit
  const offset = (page - 1) * limit;

  const isDesc = order === "desc" ? true : false;

  let resources;

  if (!keyword) {
    resources = await sql`
        SELECT * FROM resource
        ${
          isDesc
            ? sql`ORDER BY ${sql(sort)} DESC`
            : sql`ORDER BY ${sql(sort)} ASC`
        }
        OFFSET ${offset}
        LIMIT ${limit}
        `;
  } else {
    resources = await sql`
        SELECT * FROM resource 
        WHERE name ILIKE ${"%" + keyword + "%"}
        OR categories::text ILIKE ${"%" + keyword + "%"}
        OR description ILIKE ${"%" + keyword + "%"}
        ${
          isDesc
            ? sql`ORDER BY ${sql(sort)} DESC`
            : sql`ORDER BY ${sql(sort)} ASC`
        }
        OFFSET ${offset}
        LIMIT ${limit}
        `;
  }

  return resources;
};

// get total number of resources

const getTotalResources = async () => {
  const totalRekts = await sql`
    SELECT COUNT(*) FROM resource`;
  return totalRekts;
};

// get last rekt
const lastAddedResource = async () => {
  const lastAddedRekt = await sql`
    SELECT * FROM resource ORDER BY id DESC LIMIT 1`;
  return lastAddedRekt;
};

// bet unique categories count
const getUniqueCategories = async () => {
  const uniqueCategories = await sql`
    SELECT COUNT(DISTINCT value) 
    FROM resource, json_array_elements_text(resource.categories) AS value`;
  return uniqueCategories;
};

// get total keyword hits
const getTotalKeywordHits = async (keyword: string) => {
  const totalKeywordHits = await sql`
    SELECT COUNT(*)  FROM resource 
    WHERE categories::text ILIKE ${"%" + keyword + "%"}
    OR description ILIKE ${"%" + keyword + "%"}
      `;
  return totalKeywordHits;
};

//stats
const getStats = async () => {
  const totalResources = await getTotalResources();
  const totalCategories = await getUniqueCategories();
  const lastRecord = await lastAddedResource();

  return {
    totalResources,
    totalCategories,
    lastRecord,
  };
};

export const ResourceService = {
  getResources,
  getTotalResources,
  getTotalKeywordHits,
  getStats,
};
