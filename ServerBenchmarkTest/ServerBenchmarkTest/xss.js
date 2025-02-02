
document.querySelector('#send').addEventListener ('click', send);
function send() 
{
    var input = document.getElementById('xss').value;
    document.querySelector('#out').innerHTML = "<p>"+input+"</p>";
}