document.getElementById("registrationForm").addEventListener("submit", function(event) {
    event.preventDefault();
    
    const firstname = document.getElementById("firstname").value.trim();
    const lastname = document.getElementById("lastname").value.trim();
    const email = document.getElementById("email").value.trim();
    const phone = document.getElementById("phone").value.trim();
    const password = document.getElementById("password").value.trim();
    const confirmPassword = document.getElementById("confirm-password").value.trim();
    
    const nameRegex = /^[A-Za-z]+$/;
    const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    const phoneRegex = /^\d{10}$/;
    const passwordRegex = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[^A-Za-z\d])[A-Za-z\d!@#$%^&*()_+={}\[\]:;<>,.?/~`|-]{8,}$/;
    
    if (!nameRegex.test(firstname) || !nameRegex.test(lastname)) return alert("First and last name should contain only letters.");
    if (!emailRegex.test(email)) return alert("Enter a valid email address.");
    if (!phoneRegex.test(phone)) return alert("Phone number must be 10 digits.");
    if (!passwordRegex.test(password)) return alert("Password must be at least 8 characters long and contain at least one letter, one number, and one special character.");
    if (password !== confirmPassword) return alert("Passwords do not match.");
    
    alert("Registration successful!");
    document.getElementById("registrationForm").reset();
});