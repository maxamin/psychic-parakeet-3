from django.urls import path
from apps.reporting import views


app_name = "reporting"


urlpatterns = [
    path('reports/', views.ReportList.as_view(), name="report-list"),
    path('reports/create/', views.ReportCreate.as_view(), name="report-create"),
    path('reports/<str:pk>/', views.ReportManagementSummary.as_view(), name="report-detail"),
    path('reports/<str:pk>/delete/', views.ReportDelete.as_view(), name="report-delete"),

    path('reports/<str:pk>/releases/', views.ReportReleaseList.as_view(), name="report-release-list"),
    path('reports/<str:pk>/releases/create/', views.ReportReleaseCreate.as_view(), name="report-release-create"),
    path('reports/<str:pk>/releases/wip-create/', views.ReportReleaseWIPCreate.as_view(),
         name="report-release-wip-create"),

    path('reports/<str:pk>/versions/', views.VersionList.as_view(), name="version-list"),
    path('reports/<str:pk>/versions/create/', views.VersionCreate.as_view(), name="version-create"),

    path('reportreleases/<str:pk>/', views.ReportReleaseDetail.as_view(), name="report-release-detail"),
    path('reportreleases/<str:pk>/update/', views.ReportReleaseUpdate.as_view(), name="report-release-update"),
    path('reportreleases/<str:pk>/delete/', views.ReportReleaseDelete.as_view(), name="report-release-delete"),

    path('versions/<str:pk>/update/', views.VersionUpdate.as_view(), name="version-update"),
    path('versions/<str:pk>/delete/', views.VersionDelete.as_view(), name="version-delete"),
]
