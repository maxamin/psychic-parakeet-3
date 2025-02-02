const hre = require("hardhat")

const tokens = (n) => {
  return ethers.utils.parseUnits(n.toString(), 'ether')
}

async function main() {
  // Setup accounts & variables
  const [deployer] = await ethers.getSigners()
  const NAME = "Ticket"
  const SYMBOL = "BT"

  // Deploy contract
  const Ticket = await ethers.getContractFactory("Ticket")
  const ticket = await Ticket.deploy(NAME, SYMBOL)
  await ticket.deployed()

  console.log(`Deployed Ticket Contract at: ${ticket.address}\n`)

  // List 6 events
  const occasions = [
    {
      name: "Matic Banglore",
      cost: tokens(1),
      tickets: 0,
      date: "Nov 31",
      time: "6:00PM IST",
      location: "Mangluru , India"
    },
    {
      name: "ETH Global Mumbai",
      cost: tokens(1.5),
      tickets: 125,
      date: "Oct 23",
      time: "11:00AM IST",
      location: "Mumbai, India"
    },
    {
      name: "Starnet Pune",
      cost: tokens(1),
      tickets: 125,
      date: "Sept 26",
      time: "1:00PM IST",
      location: "Vimanagar, India"
    },
    {
      name: "Hyperledger Hackathon",
      cost: tokens(0.25),
      tickets: 200,
      date: "nov 9",
      time: "10:00AM Ist",
      location: "Delhi, India"
    },
    {
      name: "F1 racing",
      cost: tokens(5),
      tickets: 0,
      date: "Jan 11",
      time: "2:30PM IST",
      location: "American Airlines Center - Dallas, TX"
    }
   
  ]

  for (var i = 0; i < 5; i++) {
    const transaction = await ticket.connect(deployer).list(
      occasions[i].name,
      occasions[i].cost,
      occasions[i].tickets,
      occasions[i].date,
      occasions[i].time,
      occasions[i].location,
    )

    await transaction.wait()

    console.log(`Listed Event ${i + 1}: ${occasions[i].name}`)
  }
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});

// 0x5FbDB2315678afecb367f032d93F642f64180aa3