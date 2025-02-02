import { sql } from "./db.ts";

// based on product or company id, check if the user has already voted
const hasVoted = async (address: string, id: number, type: string) => {
  const voteResult = await sql`
            SELECT * FROM vote
            WHERE user_address = ${address}
            AND voteable_id = ${id}
            AND voteable_type = ${type}
        `;

  return voteResult.length > 0 ? true : false;
};

// add the vote to the database should be always called after hasVoted
const castVote = async (address: string, id: number, type: string) => {
  const result = await sql`
        INSERT INTO vote (user_address, voteable_id, voteable_type)
        VALUES (${address}, ${id}, ${type})
    `;

  // update the votes count in the company/product table
  await sql`
        UPDATE ${sql(type)} SET up_votes = up_votes + 1
        WHERE id = ${id}
    `;

  return result;
};

export const VotesService = {
  hasVoted,
  castVote,
};
