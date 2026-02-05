/* ================= RIPPLE EFFECT ================= */

document.querySelectorAll(".ripple-btn").forEach(btn => {
  btn.addEventListener("click", function(e){

    const circle = document.createElement("span");
    circle.classList.add("ripple");

    const rect = btn.getBoundingClientRect();

    circle.style.left = e.clientX - rect.left + "px";
    circle.style.top  = e.clientY - rect.top + "px";

    btn.appendChild(circle);

    setTimeout(()=>circle.remove(),600);
  });
});



/* ================= SCROLL REVEAL ================= */

const reveals = document.querySelectorAll(".reveal");

function revealOnScroll(){
  reveals.forEach((el, index)=>{
    const top = el.getBoundingClientRect().top;

    if(top < window.innerHeight - 100){
      setTimeout(()=>{
        el.classList.add("active");
      }, index * 150);   // stagger animation
    }
  });
}

window.addEventListener("scroll", revealOnScroll);
revealOnScroll();



/* ================= ANIMATED COUNTERS ================= */

const counters = document.querySelectorAll(".stat h3");

function runCounter(counter){

  const target = parseInt(counter.innerText.replace(/[^0-9]/g,''));
  let count = 0;

  const speed = target / 60;

  const update = () => {
    count += speed;

    if(count < target){
      counter.innerText = Math.floor(count) + "+";
      requestAnimationFrame(update);
    } else {
      counter.innerText = target + "+";
    }
  };

  update();
}

function startCounters(){
  counters.forEach(counter=>{
    const top = counter.getBoundingClientRect().top;

    if(top < window.innerHeight - 50 && !counter.classList.contains("started")){
      counter.classList.add("started");
      runCounter(counter);
    }
  });
}

window.addEventListener("scroll", startCounters);



/* ================= PARALLAX BACKGROUND ================= */

window.addEventListener("scroll", ()=>{
  const offset = window.scrollY * 0.4;
  document.body.style.backgroundPosition = `center ${offset}px`;
});
