<!--
$Revision$

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

<%@ Page Language="C#" AutoEventWireup="true" CodeBehind="Main.aspx.cs" Inherits="VulnApp.MainPage" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
	<head>
		<title>Main</title>
	</head>
	<body>
		<form id="Form1" method="post" runat="server">
			<p><asp:label style="Z-INDEX: 0" id="LoggedInLabel" runat="server" Font-Size="XX-Large" Height="48px" Width="200px">Logged In</asp:label></p>
			<p><asp:Image style="Z-INDEX: 0" id="ProfileImage" runat="server"></asp:Image> <asp:Label style="Z-INDEX: 0" id="NameLabel" runat="server"></asp:Label></p>
			<p><asp:hyperlink style="Z-INDEX: 0" id="EditProfileHyperLink" runat="server" Height="24px" Width="72px" NavigateUrl="EditProfile.aspx">Edit Profile</asp:hyperlink></p>
			<p><asp:HyperLink id="ListUsersHyperLink" runat="server" NavigateUrl="ListUsers.aspx" Visible="False">List Users</asp:HyperLink></p>
			<p><asp:HyperLink id="ScoreBoardHyperLink" runat="server" NavigateUrl="ScoreBoard.aspx">ScoreBoard</asp:HyperLink></p>
			<p><asp:HyperLink style="Z-INDEX: 0" id="HigherOrLowerHyperLink" runat="server" NavigateUrl="HigherOrLower.aspx">Higher Or Lower</asp:HyperLink></p>
			<p><asp:HyperLink id="HelpHyperLink" runat="server" NavigateUrl="LoadHelp.aspx?filename=SGVscC5odG1s">Help</asp:HyperLink></p>
		</form>
	</body>
</html>
