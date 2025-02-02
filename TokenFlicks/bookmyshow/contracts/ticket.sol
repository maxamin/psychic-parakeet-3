//SPDX-License-Identifier:MIT
pragma solidity ^0.8.0;
import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
contract Ticket is ERC721{
    address public owner;
    uint public totalOccasions;
    uint public totalSupply;
    struct Occasion{
        uint id;
        string name;
        uint cost;
        uint tickets;
        uint maxTickets;
        string date;
        string time;
        string location;
    }

    constructor(string memory _name, string memory _symbol)ERC721(_name,_symbol){
      owner=msg.sender;
    }

    mapping(uint=>mapping(address=>bool))public hasBought;
    mapping(uint=>Occasion)public allOccasion;
    mapping(uint=>mapping(uint=>address))public seatBook;
    mapping(uint=>uint[])public seatTaken;
    modifier onlyOwner(){
        require(owner==msg.sender,"Your not owner");
        _;  //it takes funtion body
    }

    function list(
    string memory _name,
    uint256 _cost,
    uint256 _maxTickets,
    string memory _date,
    string memory _time,
    string memory _location

    )public onlyOwner {
        
        // totalOccasions=totalOccasions+1;
        totalOccasions++;
        allOccasion[totalOccasions]=Occasion(totalOccasions,
        _name,
        _cost,
        _maxTickets,
        _maxTickets,
        _date,
        _time,
        _location);
    }

    function mint(uint _id,uint _seat) public payable {
        require(_id!=0);
        require(_id<=totalOccasions,"only seat avalaible");

        require(msg.value>=allOccasion[_id].cost,"amount is not matching to price");
        require(seatBook[_id][_seat]==address(0));
        require(_seat<=allOccasion[_id].maxTickets,"seat limit exceeds");
        allOccasion[_id].tickets-=1;  //when buy 1 get minus
        hasBought[_id][msg.sender] = true;   // update buying status
        seatBook[_id][_seat]=msg.sender; // asign seat
        seatTaken[_id].push(_seat);    // updates seats currently taken
        totalSupply++;
        _safeMint(msg.sender, totalSupply);
    }

    function getOccasion(uint _id)public view returns(Occasion memory){
        return allOccasion[_id];
    }

    function getSeatTaken(uint _id)public view returns(uint256[] memory){
        return seatTaken[_id];
    }

    function withdraw() public onlyOwner{
        
        (bool success, ) = owner.call{ value: address(this).balance}("");
        require(success);
    }



}