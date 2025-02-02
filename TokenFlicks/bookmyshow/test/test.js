const { expect } = require("chai");
// const { ethers } = require("ethers");
// const { ethers } = require("hardhat"); // ethers.getSigners is not function error
const NAME = "Ticket";
const SYMBOL = "BT";

const OCCASION_NAME = "Matic Banglore";
const OCCASION_COST =  ethers.utils.parseUnits("1", "ether");  //ethers.utils.parseUnits has been use for ethers version below 6; 
// above 6 use ethers.parseuints
const OCCASION_MAX_TICKETS = 100;
const OCCASION_DATE = "27 sept 2023";
const OCCASION_TIME = "10:00AM IST";
const OCCASION_LOCATION = "banglore, India";

describe("Ticket", () => {
  let ticket;
  let deployer, buyer;
  //run common code before ceetain test example
  beforeEach(async () => {
    [deployer, buyer] = await ethers.getSigners();
    const Ticket = await ethers.getContractFactory("Ticket");
    ticket = await Ticket.deploy(NAME, SYMBOL);
    
    

   const transaction= await ticket
      .connect(deployer)
      .list(
        OCCASION_NAME,
        OCCASION_COST,
        OCCASION_MAX_TICKETS,
        OCCASION_DATE,
        OCCASION_TIME,
        OCCASION_LOCATION
    );
    //getting tadded to block thats why wait
    await transaction.wait()

  });
  describe("deployment", () => {
    //checks test pases or not
    it("Sets name", async () => {
      let name = await ticket.name();
      expect(name).to.equal(NAME);
      // expect(await ticket.name()).to.equal("Ticket")
    });

    it("sets the symbol", async () => {
      let symbol = await ticket.symbol();
      expect(symbol).to.equal(SYMBOL);
    });
    

    //to check owner
    it("Sets the owner", async () => {
      let Owner = await ticket.owner(); // small owner is giving (ticket.owner is not a function)
      expect(Owner).to.equal(deployer.address);
    });
  });

  describe("Occasions", () => {
    it("update the counts of occasion", async () => {
      const TotalOccasions = await ticket.totalOccasions();
      expect(TotalOccasions).to.be.equal(1);
    });

    it("return occasions value", async()=>{
      //get error when getOccasion function taking input _id is only id in contract
        const occasion = await ticket.getOccasion(1);
        expect(occasion.id).to.be.equal(1);
        expect(occasion.name).to.be.equal(OCCASION_NAME);
        expect(occasion.cost).to.be.equal(OCCASION_COST);
        expect(occasion.tickets).to.be.equal(OCCASION_MAX_TICKETS);
        expect(occasion.date).to.be.equal(OCCASION_DATE);
        expect(occasion.time).to.be.equal(OCCASION_TIME);
        expect(occasion.location).to.be.equal(OCCASION_LOCATION);
    })
  });

  describe("Minting",()=>{
    const ID=1;
    const SEAT=50;
    const AMOUNT=ethers.utils.parseUnits('1', 'ether')

    beforeEach(async()=>{
      const transaction = await ticket.connect(buyer).mint(ID,SEAT,{value:AMOUNT})
      await transaction.wait()
    })
    it('update ticket count', async()=>{
      const occasion = await ticket.getOccasion(1)
      expect(occasion.tickets).to.be.equal(OCCASION_MAX_TICKETS - 1)
    })
    it("buying status", async()=>{
      const check= await ticket.hasBought(ID, buyer.address)
      expect(check).to.be.true
      // expect(check).to.be.equal(true)  dont use this this gives error
    })
    it("Updated seat status",async()=>{
      const owner=await ticket.seatBook(ID,SEAT)
      expect(owner).to.equal(buyer.address)
    })

    it("update overall setaing status",async()=>{
      const seats= await ticket.getSeatTaken(ID)
      expect(seats.length).to.equal(1)
      expect(seats[0]).to.equal(SEAT)
    })

    it('updates contract balance', async()=>{
      
      const balances = await ethers.provider.getBalance(ticket.address)
      // const balances = await ticket.balanceOf(ticket)
      // console.log(balances)
      console.log("Contract deployed to:", ticket.address);
      console.log("Contract deployed to:", balances);
      expect(balances).to.be.equal(AMOUNT)
    })

  })

  describe("withdraw",()=>{
    const ID=1;
    const SEAT=50;
    const AMOUNT=ethers.utils.parseUnits('1', 'ether')
    let balanceBefore

    beforeEach(async()=>{
      balanceBefore=await ethers.provider.getBalance(deployer.address)
      let transaction = await ticket.connect(buyer).mint(ID,SEAT,{value:AMOUNT})
      await transaction.wait();

      transaction=await ticket.connect(deployer).withdraw()
      await transaction.wait()
    })
    it("update owner balance", async()=>{
      const balancesAfterWithdrawal = await ethers.provider.getBalance(deployer.address)
      expect(balancesAfterWithdrawal).to.be.greaterThan(balanceBefore)
    })
    it('Updates the contract balance', async () => {
      const balance = await ethers.provider.getBalance(ticket.address)
      expect(balance).to.equal(0)
    })
  })
  
})
