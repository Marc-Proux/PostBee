# Generated by Django 3.2.19 on 2023-06-15 09:32

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('api_postBee', '0001_initial'),
    ]

    operations = [
        migrations.AlterField(
            model_name='comment',
            name='text',
            field=models.CharField(max_length=250),
        ),
    ]
