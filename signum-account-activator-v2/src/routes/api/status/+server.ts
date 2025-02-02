import { messageResponse } from '$lib/server/messageResponse';
export function GET() {
	return new Response(messageResponse('Running fine...'));
}
