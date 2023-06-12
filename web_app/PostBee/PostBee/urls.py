"""PostBee URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/3.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path, include
from rest_framework_simplejwt.views import (TokenObtainPairView, TokenRefreshView)
from rest_framework import routers

from api_postBee.views import *

postRouter = routers.SimpleRouter()
postRouter.register('posts', PostList, basename='post')
postRouter.register('post', PostDetail, basename='postDetail')

urlpatterns = [
    path('admin/', admin.site.urls),
    # path('api/token/', TokenObtainPairView.as_view(), name='token_obtain_pair'),
    # path('api/token/refresh/', TokenRefreshView.as_view(), name='token_refresh'),
    path('login', LoginView.as_view(), name='login'),
    path('register', RegisterView.as_view(), name='register'),
    path('activate/<uidb64>/<token>', ActivateAccount.as_view(), name='activate'),
    path('', IndexView.as_view(), name='index'),
    path('', include(postRouter.urls)),
    path('publish', PublishPost.as_view(), name='publish'),
    path('comment', PublishComment.as_view(), name='comment'),
    path('approve', ApprovePost.as_view(), name='approve'),
    path('delete_user', DeleteUser.as_view(), name='delete_user'),
    path('add_modo', AddModo.as_view(), name='add_modo'),
    path('delete_comment', DeleteComment.as_view(), name='delete_comment'),
    path('user_info', UserView.as_view(), name='get_user'),
    path('refresh_token', TokenRefresh.as_view(), name='refresh_token'),
]
