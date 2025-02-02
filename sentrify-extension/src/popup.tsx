import { BadgeCheck, BadgeX, Github, Loader, Twitter } from "lucide-react";
import React, { useEffect, useState } from "react";
import { createRoot } from "react-dom/client";
import "./App.css";


interface IProjects {
  name: string;
  website: string;
  category: string;
  chain: string;
  twitter: string;
}

type fetchStatus = "initial" | "fetching" | "error" | "success" | "not-found";

const Popup = () => {
  const [currentURL, setCurrentURL] = useState<string>();
  const [urlData, setUrlData] = useState<IProjects | null>(null);
  const [fetchStatus, setFetchStatus] = useState<fetchStatus>("initial")

  useEffect(() => {
    if(fetchStatus === "success") {
      chrome.action.setBadgeText({ text: "G" });
      chrome.action.setBadgeBackgroundColor({ color: "#2ECC71" });
    } else if (fetchStatus === "error" || fetchStatus === "not-found") {
      chrome.action.setBadgeText({ text: "X" });
      chrome.action.setBadgeBackgroundColor({ color: "#C70039" });
    } 
  }, [fetchStatus]);

  useEffect(() => {
    chrome.tabs.query({ active: true, currentWindow: true }, function (tabs) {
      const currentSiteUrl = tabs[0].url;
      if(currentSiteUrl) {
        const urlObj = new URL(currentSiteUrl!); 
        verifyURL(urlObj.origin)
        setCurrentURL(urlObj.host);
      } else {
        alert("Open Extension In a Valid Website")
      }
    });
  }, []);

  const maskUrl = (str: string): string => {
    const newStr = (str = str
      .replace(/:/g, "&")
      .replace(/\//g, "$")
      .replace(/\./g, "*"));
    return newStr;
}

  const verifyURL = async (url: string) => {
    setFetchStatus("fetching");
    const mastUrl = maskUrl(url)
    try {
      const response = await fetch(
        `https://sentrify-app.vercel.app/api/projects/${mastUrl}`
      );
      if (!response.ok) {
        setFetchStatus("not-found");
        return;
      }
      const data = await response.json();
      setUrlData(data);
      setFetchStatus("success");
    } catch (error) {
      setFetchStatus("error")
    }
  }

  const Heading = (
    <div className="info">
      <h1>Sentrify.</h1>
      <p>
        Verify the <span className="highlight">authenticity</span> of any Web3
        Dapp url
      </p>
    </div>
  );

  const Footer = (
    <div className="footer">
      <a href="https://sentrify-app.vercel.app/request" target="_blank" className="request_btn">
        Request Dapp
      </a>
      <div className="resources">
        <p>version 1.0</p>
        <a href="/" className="source">
          <Github />
        </a>
      </div>
    </div>
  );


  if(fetchStatus === "fetching") {
    return (
      <div className="main">
        {Heading}
        <div className="fetching">
          <p>Verifying {currentURL}...</p>
          <Loader className="loader" />
        </div>

        {Footer}
      </div>
    );
  }

  if(fetchStatus === "not-found") {
    return (
      <div className="main">
        {Heading}
        <div className="not_found">
          <div className="">
            <BadgeX size={34} className="x_icon" />
            <p>
              {" "}
              <span className="link_highlight">{currentURL}</span> does not appear to be an authentic web3
              website
            </p>
          </div>
          <p>
            If you believe it is an authentic web3 website, please submit a request
            below.
          </p>
        </div>
        {Footer}
      </div>
    );
  }


  return (
    <div className="main">
      {Heading}

      {urlData && (
        <div className="data">
          <div className="">
            <BadgeCheck size={34} className="check" />
            <p>
              <span className="link_highlight">{urlData.website}</span> is an authentic web3 website
            </p>
          </div>
          <div className="twitter">
            <p>
              Checkout {urlData.name}'s twitter account if you're still unsure
            </p>
            <a href={urlData.twitter} target="_blank" className="twitter_link">
              <Twitter />
            </a>
          </div>
        </div>
      )}

      {Footer}
    </div>
  );
};

const root = createRoot(document.getElementById("root")!);

root.render(
  <React.StrictMode>
    <Popup />
  </React.StrictMode>
);
