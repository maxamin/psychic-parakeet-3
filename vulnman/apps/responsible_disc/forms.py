from django import forms
from django.conf import settings
from django.urls import reverse_lazy
from crispy_forms.helper import FormHelper
from crispy_forms import layout
from crispy_forms.bootstrap import FormActions
from crispy_bootstrap5 import bootstrap5
from vulnman.core.forms import CodeMirrorWidget, FileDropWidget
from vulnman.core.forms import DateInput
from apps.responsible_disc import models
from django.contrib.auth.forms import PasswordResetForm


class TextProofForm(forms.ModelForm):
    class Meta:
        model = models.TextProof
        fields = ["name", "description", "text"]
        widgets = {
            "text": CodeMirrorWidget(),
            "description": CodeMirrorWidget()
        }

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.helper = FormHelper()
        self.helper.layout = layout.Layout(
            layout.Row(
                bootstrap5.FloatingField("name", wrapper_class="col-sm-12"),
                bootstrap5.Field("description", wrapper_class="col-sm-12"),
                bootstrap5.Field("text", wrapper_class="col-sm-12"),
                css_class="g-2"
            ),
            layout.Row(
                FormActions(layout.Submit("submit", "Submit", css_class="btn btn-primary w-100"),
                            wrapper_class="col-sm-12 col-md-6")
            )
        )


class ImageProofForm(forms.ModelForm):
    class Meta:
        model = models.ImageProof
        fields = ["name", "description", "image", "caption"]
        widgets = {
            "description": CodeMirrorWidget(),
            "image": FileDropWidget()
        }

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.helper = FormHelper()
        self.helper.layout = layout.Layout(
            layout.Row(
                bootstrap5.FloatingField("name", wrapper_class="col-sm-12"),
                bootstrap5.FloatingField("caption", wrapper_class="col-sm-12"),
                bootstrap5.Field("description", wrapper_class="col-sm-12"),
                bootstrap5.Field("image", wrapper_class="col-sm-12"),
                css_class="g-2"
            ),
            layout.Row(
                FormActions(layout.Submit("submit", "Submit", css_class="btn btn-primary w-100"),
                            wrapper_class="col-sm-12 col-md-6")
            )
        )


def get_template_choices():
    choices = []
    for choice in settings.RESPONSIBLE_DISCLOSURE_ADVISORY_TEMPLATES.keys():
        choices.append((choice, choice))
    return choices


class VulnerabilityForm(forms.ModelForm):
    template_id = forms.CharField(label="Template", widget=forms.Select())
    advisory_template = forms.ChoiceField(label="Advisory Template", choices=get_template_choices())

    class Meta:
        model = models.Vulnerability
        widgets = {
            "date_planned_disclosure": DateInput()
        }
        fields = [
            "template_id", "name", "status", "cve_id", "severity", "cve_request_id", "vendor", "vendor_homepage",
            "vendor_email", "is_fixed", "is_published", "advisory_template",
            "fixed_version", "affected_versions", "affected_product", "date_planned_disclosure"]

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.helper = FormHelper()
        self.helper.layout = layout.Layout(
            layout.Row(
                layout.Div(
                    bootstrap5.FloatingField("template_id"), css_class="col-sm-12",
                ),
            ),
            layout.Row(
                layout.Div(
                    bootstrap5.FloatingField("name"), css_class="col-sm-12",
                ),
            ),
            layout.Row(
                layout.Div(
                    bootstrap5.FloatingField("status"), css_class="col-sm-12 col-md-6",
                ),
                layout.Div(
                    bootstrap5.FloatingField("severity"), css_class="col-sm-12 col-md-6",
                ),
            ),
            layout.Row(
                layout.Div(
                    bootstrap5.FloatingField("vendor"), css_class="col-sm-12 col-md-6",
                ),
                layout.Div(
                    bootstrap5.FloatingField('affected_product'), css_class="col-sm-12 col-md-6"
                ),
            ),
            layout.Row(
                layout.Div(
                    bootstrap5.FloatingField("vendor_email"), css_class="col-sm-12 col-md-6",
                ),
                layout.Div(
                    bootstrap5.FloatingField('vendor_homepage'), css_class="col-sm-12 col-md-6",
                )
            ),
            layout.Row(
                layout.Div(
                    bootstrap5.FloatingField('affected_versions'), css_class="col-sm-12"
                ),
            ),
            layout.Row(
                layout.Div(
                    bootstrap5.FloatingField('fixed_version'), css_class="col-sm-12"
                )
            ),
            layout.Row(
                layout.Div(
                    bootstrap5.FloatingField("cve_id"), css_class="col-sm-12 col-md-6",
                ),
                layout.Div(bootstrap5.FloatingField("cve_request_id"), css_class="col-sm-12 col-md-6"),
            ),
            layout.Row(
                layout.Div(
                    bootstrap5.Field("is_fixed"), css_class="col-sm-12 col-md-6"
                ),
                layout.Div(
                    bootstrap5.Field("is_published"), css_class="col-sm-12 col-md-6"
                )
            ),
            layout.Row(
                layout.Div(
                    bootstrap5.FloatingField("advisory_template"), css_class="col-sm-12"
                )
            ),
            layout.Row(
                layout.Div(bootstrap5.FloatingField("date_planned_disclosure"), css_class="col-sm-12")
            ),
            layout.Row(
                FormActions(layout.Submit("submit", "Submit", css_class="btn btn-primary w-100"),
                            wrapper_class="col-sm-12 col-md-6")
            )
        )


class VulnerabilityLogForm(forms.ModelForm):
    class Meta:
        model = models.VulnerabilityLog
        fields = ["custom_date", "action", "message"]
        widgets = {
            'custom_date': DateInput
        }

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.helper = FormHelper()
        self.helper.form_tag = False
        self.helper.layout = layout.Layout(
            layout.Row(
                layout.Div(
                    bootstrap5.FloatingField("custom_date"), css_class="col-sm-12 col-md-6",
                ),
                layout.Div(
                    bootstrap5.FloatingField('action'), css_class="col-sm-12 col-md-6",
                )
            ),
            layout.Row(
                layout.Div(
                    bootstrap5.FloatingField("message"), css_class="col-sm-12",
                ),
            ),
            layout.Row(
                FormActions(layout.Submit("submit", "Submit", css_class="btn btn-primary w-100"),
                            wrapper_class="col-sm-12 col-md-6")
            )
        )


class NewCommentForm(forms.ModelForm):
    class Meta:
        model = models.VulnerabilityComment
        fields = ["text"]
        widgets = {
            "text": CodeMirrorWidget()
        }

    def __init__(self, vuln, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.fields['text'].label = False
        self.helper = FormHelper()
        self.helper.form_action = reverse_lazy("responsible_disc:comment-create", kwargs={"pk": vuln.pk})
        self.helper.layout = layout.Layout(
            layout.Row(
                bootstrap5.Field("text", wrapper_class="col-sm-12")
            ),
            layout.Row(
                FormActions(layout.Submit("submit", "Comment", css_class="btn btn-primary w-100"),
                            wrapper_class="col-sm-12 col-md-6")
            )
        )


class InviteVendorForm(PasswordResetForm):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.helper = FormHelper()
        self.helper.layout = layout.Layout(
            layout.Row(
                bootstrap5.FloatingField("email", wrapper_class="col-sm-12"),
                css_class="g-2"
            ),
            layout.Row(
                FormActions(layout.Submit("submit", "Submit", css_class="btn btn-primary w-100"),
                            wrapper_class="col-sm-12 col-md-6")
            )
        )


class UnshareVulnerability(forms.Form):
    email = forms.EmailField()

    class Meta:
        fields = ["email"]


class VulnerabilityExportForm(forms.Form):
    template = forms.ChoiceField(choices=get_template_choices())

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.helper = FormHelper()
        self.helper.layout = layout.Layout(
            layout.Row(
                bootstrap5.FloatingField("template", wrapper_class="col-sm-12"), css_class="g-2"
            ),
            layout.Row(
                FormActions(layout.Submit("submit", "Submit", css_class="btn btn-primary w-100"),
                            wrapper_class="col-sm-12 col-md-6")
            )
        )