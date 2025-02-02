<!--
$Release$

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.
* Neither the name of the Nth Dimension nor the names of its contributors may
be used to endorse or promote products derived from this software without
specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

(c) Tim Brown, 2011
<mailto:timb@nth-dimension.org.uk>
<http://www.nth-dimension.org.uk/> / <http://www.machine.org.uk/>
-->

<%@ Page Language="C#" AutoEventWireup="true" CodeBehind="Default.aspx.cs" Inherits="VulnApp._Default" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
	<head>
		<title>Welcome</title>
	</head>
	<body>
		<form id="Form1" method="post" runat="server">
			<p><asp:label style="Z-INDEX: 0" id="WelcomeLabel" runat="server" Width="200px" Height="56px" Font-Size="XX-Large">Welcome!</asp:label></p>
			<p><asp:label style="Z-INDEX: 0" id="UsernameLabel" runat="server" Width="64px" Height="16px">Username</asp:label><asp:textbox style="Z-INDEX: 0" id="UsernameTextBox" runat="server" Width="280px"></asp:textbox></p>
			<p><asp:label style="Z-INDEX: 0" id="PasswordLabel" runat="server" Width="64px" Height="16px">Password</asp:label><asp:textbox style="Z-INDEX: 0" id="PasswordTextBox" runat="server" Width="280px"></asp:textbox></p>
			<p><asp:button style="Z-INDEX: 0" id="LoginButton" runat="server" Width="40px" Height="24px" Text="Login" onclick="LoginButton_Click"></asp:button></p>
			<p><asp:Label style="Z-INDEX: 0" id="ErrorLabel" runat="server"></asp:Label></p>
		</form>
	</body>
</html>
