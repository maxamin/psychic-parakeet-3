from django.http import HttpResponse, HttpRequest


def index(request: HttpRequest) -> HttpResponse:
    name: str = request.GET.get("name", "User")
    return HttpResponse(f"Hello, {name}")
