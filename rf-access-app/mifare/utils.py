"""
Utility functions for MIFARE card operations

Provides helper functions for data formatting, display, and common operations.
"""

import binascii
from typing import Dict, Any, List, Optional
from colorama import Fore, Style

class MifareUtils:
    """Utility functions for MIFARE operations"""
    
    @staticmethod
    def hex_to_bytes(hex_string: str) -> bytes:
        """Convert hex string to bytes"""
        return binascii.unhexlify(hex_string.replace(' ', '').replace(':', ''))
    
    @staticmethod
    def bytes_to_hex(data: bytes, separator: str = ' ') -> str:
        """Convert bytes to hex string"""
        return separator.join(f'{b:02X}' for b in data)
    
    @staticmethod
    def format_uid(uid: str) -> str:
        """Format UID for display"""
        if not uid:
            return "Unknown"
        
        # Remove spaces and convert to uppercase
        clean_uid = uid.replace(' ', '').upper()
        
        # Add colons every 2 characters
        formatted = ':'.join(clean_uid[i:i+2] for i in range(0, len(clean_uid), 2))
        return formatted
    
    @staticmethod
    def format_atr(atr: str) -> str:
        """Format ATR for display"""
        if not atr:
            return "Unknown"
        
        # Remove spaces and convert to uppercase
        clean_atr = atr.replace(' ', '').upper()
        
        # Add spaces every 2 characters
        formatted = ' '.join(clean_atr[i:i+2] for i in range(0, len(clean_atr), 2))
        return formatted
    
    @staticmethod
    def display_card_data(card_data: Dict[str, Any]) -> None:
        """Display card data in a formatted way"""
        print(f"\n{Fore.CYAN}=== Card Information ==={Style.RESET_ALL}")
        
        if 'reader' in card_data:
            print(f"{Fore.YELLOW}Reader:{Style.RESET_ALL} {card_data['reader']}")
        
        if 'type' in card_data:
            print(f"{Fore.YELLOW}Type:{Style.RESET_ALL} {card_data['type']}")
        
        if 'uid' in card_data and card_data['uid']:
            formatted_uid = MifareUtils.format_uid(card_data['uid'])
            print(f"{Fore.YELLOW}UID:{Style.RESET_ALL} {formatted_uid}")
        
        if 'atr' in card_data and card_data['atr']:
            formatted_atr = MifareUtils.format_atr(card_data['atr'])
            print(f"{Fore.YELLOW}ATR:{Style.RESET_ALL} {formatted_atr}")
        
        if 'memory_size' in card_data and card_data['memory_size']:
            print(f"{Fore.YELLOW}Memory:{Style.RESET_ALL} {card_data['memory_size']} bytes")
        
        if 'applications' in card_data and card_data['applications']:
            print(f"{Fore.YELLOW}Applications:{Style.RESET_ALL}")
            for app in card_data['applications']:
                print(f"  - {app}")
        
        print()
    
    @staticmethod
    def validate_hex_string(hex_string: str) -> bool:
        """Validate if string is valid hexadecimal"""
        try:
            int(hex_string.replace(' ', '').replace(':', ''), 16)
            return True
        except ValueError:
            return False
    
    @staticmethod
    def calculate_checksum(data: bytes) -> int:
        """Calculate simple checksum for data"""
        return sum(data) & 0xFF
    
    @staticmethod
    def split_hex_string(hex_string: str, chunk_size: int = 16) -> List[str]:
        """Split hex string into chunks for display"""
        clean_hex = hex_string.replace(' ', '').replace(':', '')
        chunks = []
        
        for i in range(0, len(clean_hex), chunk_size * 2):
            chunk = clean_hex[i:i + chunk_size * 2]
            formatted_chunk = ' '.join(chunk[j:j+2] for j in range(0, len(chunk), 2))
            chunks.append(formatted_chunk)
        
        return chunks
    
    @staticmethod
    def print_hex_dump(data: bytes, title: str = "Hex Dump") -> None:
        """Print hex dump of data"""
        print(f"\n{Fore.CYAN}=== {title} ==={Style.RESET_ALL}")
        
        hex_string = MifareUtils.bytes_to_hex(data, '')
        chunks = MifareUtils.split_hex_string(hex_string, 16)
        
        for i, chunk in enumerate(chunks):
            offset = f"{i * 16:04X}"
            ascii_repr = ''.join(chr(b) if 32 <= b <= 126 else '.' 
                                for b in data[i*16:(i+1)*16])
            print(f"{Fore.YELLOW}{offset}:{Style.RESET_ALL} {chunk:<47} {Fore.GREEN}|{ascii_repr}|{Style.RESET_ALL}")
        
        print()
    
    @staticmethod
    def parse_tlv(data: bytes) -> List[Dict[str, Any]]:
        """Parse TLV (Tag-Length-Value) encoded data"""
        tlv_objects = []
        i = 0
        
        while i < len(data):
            if i + 1 >= len(data):
                break
            
            tag = data[i]
            length = data[i + 1]
            
            if i + 2 + length > len(data):
                break
            
            value = data[i + 2:i + 2 + length]
            
            tlv_objects.append({
                'tag': f'{tag:02X}',
                'length': length,
                'value': MifareUtils.bytes_to_hex(value)
            })
            
            i += 2 + length
        
        return tlv_objects
    
    @staticmethod
    def format_file_size(size_bytes: int) -> str:
        """Format file size in human readable format"""
        if size_bytes < 1024:
            return f"{size_bytes} B"
        elif size_bytes < 1024 * 1024:
            return f"{size_bytes / 1024:.1f} KB"
        else:
            return f"{size_bytes / (1024 * 1024):.1f} MB"
