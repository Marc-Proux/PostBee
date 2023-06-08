from rest_framework.views import APIView
from rest_framework.viewsets import ModelViewSet
import json
from django.core.mail import EmailMessage
from django.contrib.sites.shortcuts import get_current_site
from django.template.loader import render_to_string
from django.utils.http import urlsafe_base64_encode, urlsafe_base64_decode
from django.utils.encoding import force_bytes, force_str
from rest_framework.response import Response
from rest_framework import status
from django.contrib.auth import authenticate, login
from rest_framework.views import APIView
from rest_framework_simplejwt.tokens import RefreshToken
from rest_framework.permissions import IsAuthenticated

from api_postBee.forms import RegisterForm
from api_postBee.tokens import account_activation_token
from api_postBee.models import *
from api_postBee.serializers import PostSerializer


class IndexView(APIView):
    def get(self, request, format=None):
        return Response({'message': 'Hello, world!'}, status=status.HTTP_200_OK)

class LoginView(APIView):
    def post(self, request, format=None):
        email = request.data.get('email')
        password = request.data.get('password')

        user = authenticate(request, email=email, password=password)
        if user is not None:
            login(request, user)
            print('Login complete for ' + user.email)
            token = LoginView.get_tokens_for_user(user)
            return Response(token, status=status.HTTP_200_OK)
        else:
            return Response({'error': 'Invalid credentials'}, status=status.HTTP_400_BAD_REQUEST)
        
    def get_tokens_for_user(user):
        refresh = RefreshToken.for_user(user)

        return {
            'refresh': str(refresh),
            'access': str(refresh.access_token),
        }


class RegisterView(APIView):
    def post(self, request, format=None):
        if request.method == 'POST':
            json_data = json.loads(request.body)
            form = RegisterForm(json_data)
            if form.is_valid():
                user = form.save(commit=False)
                user.is_active = False
                user.save()
                # for field in user._meta.fields:
                #     field_name = field.name
                #     field_value = getattr(user, field_name)
                #     print(f"{field_name}: {field_value}")
                self.activate_email(request, user)
                response_data = {
                    'success': True,
                    'message': 'User registration successful.'
                }
                return Response(response_data, status=status.HTTP_201_CREATED)
            else:
                errors = {field: errors[0] for field, errors in form.errors.items()}
                print(errors)
                response_data = {
                    'success': False,
                    'errors': errors
                }
                return Response(response_data, status=status.HTTP_400_BAD_REQUEST)
        else:
            response_data = {
                'success': False,
                'errors': 'Invalid request method.'
            }
            return Response(response_data, status=status.HTTP_405_METHOD_NOT_ALLOWED)


    def activate_email(self, request, user):
        mail_subject = 'Activate your user account.'
        message = render_to_string('api_postBee/template_activate_account.html', {
            'name': str(user.first_name)+" "+str(user.last_name),
            'domain': get_current_site(request).domain,
            'uid': urlsafe_base64_encode(force_bytes(user.pk)),
            'token': account_activation_token.make_token(user),
            'protocol': 'https' if request.is_secure() else 'http'
        })
        email = EmailMessage(mail_subject, message, to=[user.email])
        if email.send():
            Response({'message': 'Confirmation email sent.'}, status=status.HTTP_200_OK)
        else:
            Response({'message': 'Confirmation email not sent.'}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class ActivateAccount(APIView):
    def get(self, request, uidb64, token):
        User = Account
        try:
            uid = force_str(urlsafe_base64_decode(uidb64))
            user = User.objects.get(pk=uid)
        except (TypeError, ValueError, OverflowError, User.DoesNotExist):
            user = None

        if user is not None and account_activation_token.check_token(user, token):
            # print("I will activate the user account: " + user.username)
            user.is_active = True
            user.save()

            # print("Thank you for your email confirmation. Now you can login to your account.")
            return Response({'message': 'Thank you for your email confirmation. Now you can login to your account.'}, status=status.HTTP_200_OK)
        else:
            # print("Activation link is invalid!")
            return Response({'message': 'Activation link is invalid!'}, status=status.HTTP_400_BAD_REQUEST)


# Endpoint view that return all posts as JSON with Response only if authenticated

class PostList(ModelViewSet):
    serializer_class = PostSerializer
    queryset = Post.objects.filter(status='1')
    permission_classes = [IsAuthenticated]

    def list(self, request, *args, **kwargs):
        queryset = self.filter_queryset(self.get_queryset())
        serializer = self.get_serializer(queryset, many=True)
        return Response(serializer.data)