import { ethers } from 'ethers'

const Card = ({ occasions, toggle, setToggle, setOccasions,setCallApi }) => {
  const togglePop = () => {
    setOccasions(occasions)
    toggle ? setToggle(false) : setToggle(true)
  }

  return (
    <div className='card'>
      <div className='card__info'>
        <p className='card__date'>
          <strong>{occasions.date}</strong><br />{occasions.time}
        </p>

        <h3 className='card__name'>
          {occasions.name}
        </h3>

        <p className='card__location'>
          <small>{occasions.location}</small>
        </p>

        <p className='card__cost'>
          <strong>
          
            {/* {ethers.utils.formatUnits(occasions.cost.toString(), 'ether')} */}
            {occasions?.cost/10**18}
          </strong>
          ETH
        </p>

        {occasions?.tickets?.toString() === "0" ? (
          <button
            type="button"
            className='card__button--out'
            disabled
          >
            Sold Out
          </button>
        ) : (
          <button
            type="button"
            className='card__button'
            onClick={() => togglePop()}
          >
            View Seats
          </button>
        )}
      </div>

      <hr />
    </div >
  );
}

export default Card;