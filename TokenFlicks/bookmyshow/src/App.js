// import './App.css';
import { useEffect, useState } from "react";
import { ethers } from "ethers";
//components
import Navigation from "./components/Navigation";
import Sort from "./components/Sort";
import Card from "./components/Card";
import SeatChart from "./components/SeatChart";

//abis
import Ticket from "./abis/Ticket.json";
// import { config } from 'hardhat';
// //config
import config from "./config.json";

function App() {
  const [provider, setProvider] = useState(null);
  const [account, setAccount] = useState(null);
  const [ticket, setTicket] = useState(null);

  const [occasions, setOccasions] = useState([]);
  const [toggle, setToggle] = useState(false);

  const loadBlockchainData = async () => {
    debugger
    const provider = new ethers.providers.Web3Provider(window.ethereum);
    setProvider(provider);

    const network = await provider.getNetwork();
    console.log(network);
    const address = config[network.chainId].Ticket.address;

    const ticket = new ethers.Contract(address, Ticket, provider);
    setTicket(ticket);

    const totalOccasions = await ticket.totalOccasions();
    const occasions = [];
    for (var i = 1; i <= totalOccasions; i++) {
      const occasion = await ticket.getOccasion(i);
      occasions.push(occasion);
    }
    setOccasions(occasions);
    // console.log({occasions});

    // console.log(tokenFlicks.address)
    //fetch accounts
    // const accounts= await window.ethereum.request({method:'eth_requestAccounts'})
    // const account = ethers.utils.getAddress(accounts[0])
    // setAccount(account)
    // console.log(accounts[0]);

    //refersh account
    window.ethereum.on("accountsChanged", async () => {
      const accounts = await window.ethereum.request({
        method: "eth_requestAccounts",
      });
      const account = ethers.utils.getAddress(accounts[0]);
      setAccount(account);
    });
  };
  useEffect(() => {
    loadBlockchainData();
  }, []);
  return (
    <div>
      <header>
        <Navigation account={account} setAccount={setAccount} />
        <h2 className="header__title">
          <strong>Event</strong> Tickets
        </h2>
      </header>
      <Sort />
      <div className="cards">
        {occasions.map((occasions, index) => (
          <Card
            occasions={occasions}
            id={index + 1}
            ticket={ticket}
            provider={provider}
            account={account}
            toggle={toggle}
            setToggle={setToggle}
            setOccasions={setOccasions}
            key={index}
          />
        ))}
      </div>

      {toggle && (
        <SeatChart
          occasions={occasions}
          ticket={ticket}
          provider={provider}
          setToggle={setToggle}
        
        />
      )}
    </div>
  );
}

export default App;
