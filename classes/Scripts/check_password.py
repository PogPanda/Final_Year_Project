import hashlib
import requests
import getpass

def get_password_hash(password):
    # Hash the password using SHA-1
    sha1_password = hashlib.sha1(password.encode()).hexdigest().upper()
    return sha1_password

def check_password_in_breaches(passwordHashPrefix,passwordHashSuffix):
    api_url = f'https://api.pwnedpasswords.com/range/{passwordHashPrefix}'
    
    try:
        response = requests.get(api_url)
        response.raise_for_status()
        

        #Split the response gotten into multiple lines
        lines = response.text.splitlines()

        for line in lines:

            parts = line.split(":") 
            if len(parts) == 2:
                hash_part = parts [0]
                count = parts[1]

                if hash_part == passwordHashSuffix:
                    return int(count)
        
        
        #Password hash suffix not found in breaches
        return 0
    
    except requests.exceptions.RequestException as e:
        print(f"Error: {e}")
        return -1

def main():
    # Get a password from the user securely
    password = input("")
    
    # Hash the password and seperates the hash into a prefix and a suffix
    passwordHash = get_password_hash(password)
    passwordHashPrefix = passwordHash[:5]
    passwordHashSuffix = passwordHash[5:]

    # Check if the password is in known breaches
    count = check_password_in_breaches(passwordHashPrefix, passwordHashSuffix)
    
    if count == -1:
        print("Failed to check the password.")
    else:
        print(count)


if __name__ == "__main__":
    main()