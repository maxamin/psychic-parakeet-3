using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Fiddler;

namespace WaspToucher.Checks.Implementations.Owasp.Transport
{
    public class TransportLayerSecurityUsesSecureCookie : IPassiveCheck
    {
        public string Description
        {
            get { throw new NotImplementedException(); }
        }

        public string Name
        {
            get
            {
                return "Cookie should be marked as secure whilst using TLS";
            }
        }

        /// <summary>
        /// Gets the compliances.
        /// </summary>
        public ComplianceStandard[] Compliances
        {
            get
            {
                return new ComplianceStandard[] { ComplianceStandard.Owasp };
            }
        }

        public Uri InformationUrl
        {
            get { throw new NotImplementedException(); }
        }

        public PassiveCheckResult RunCheck(Fiddler.Session fiddlerSession)
        {
            if (fiddlerSession.isHTTPS && fiddlerSession.oResponse.headers.Exists("set-cookie"))
            {
                string cookie = fiddlerSession.oResponse.headers["set-cookie"];

                if (cookie != null && cookie.Length > 0)
                {
                    string[] parts = cookie.Split(';');
                    string cookiename = parts[0];
                    cookiename = cookiename.Split('=')[0];

                    if (parts != null && parts.Length > 0)
                    {
                        bool isSecured = false;
                        bool isDomainSet = false;

                        parts.ForEach(v =>
                            {
                                if (v.Trim().ToLower() == "secure")
                                {
                                    isSecured = true;
                                }

                                if (v.Trim().ToLower().StartsWith("domain"))
                                {
                                    isDomainSet = true;
                                }
                            });

                        if (!isSecured)
                        {
                            return PassiveCheckResult.CreateFailure(this, fiddlerSession.fullUrl, "Cookie not marked as secure");
                        }
                    }
                }
            }

            return PassiveCheckResult.CreatePass(this, fiddlerSession.fullUrl);
        }
    }
}