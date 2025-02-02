import { json, type RequestHandler } from '@sveltejs/kit';
import apiDescription from './description.openapi.json';
export const GET: RequestHandler = () => {
	return json(apiDescription);
};
