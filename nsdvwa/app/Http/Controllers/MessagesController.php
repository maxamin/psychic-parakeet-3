<?php

namespace App\Http\Controllers;

use GuzzleHttp\Middleware;
use Illuminate\Http\Request;
use App\Models\Message;

class MessagesController extends Controller
{
    public function __construct() {
        $this->middleware('auth');
    }

    public function index() {
        $user = auth()->user();
        $messages = Message::wherein('user_id', $user->id);

        return view('home.index', ['messages' => $messages]);
    }
}
