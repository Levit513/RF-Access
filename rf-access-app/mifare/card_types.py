"""
MIFARE Card Type Definitions

Defines various MIFARE card types and their characteristics.
"""

from enum import Enum
from dataclasses import dataclass
from typing import Optional, Dict, Any

class MifareCardType(Enum):
    """Enumeration of supported MIFARE card types"""
    CLASSIC_1K = "MIFARE Classic 1K"
    CLASSIC_4K = "MIFARE Classic 4K"
    ULTRALIGHT = "MIFARE Ultralight"
    ULTRALIGHT_C = "MIFARE Ultralight C"
    DESFIRE_EV1 = "MIFARE DESFire EV1"
    DESFIRE_EV2 = "MIFARE DESFire EV2"
    DESFIRE_EV3 = "MIFARE DESFire EV3"
    PLUS_S = "MIFARE Plus S"
    PLUS_X = "MIFARE Plus X"
    UNKNOWN = "Unknown MIFARE"

@dataclass
class CardInfo:
    """Information about a MIFARE card"""
    card_type: MifareCardType
    uid: Optional[str] = None
    atr: Optional[str] = None
    reader: Optional[str] = None
    memory_size: Optional[int] = None
    sector_count: Optional[int] = None
    block_count: Optional[int] = None
    applications: Optional[list] = None
    
    def __str__(self):
        return f"{self.card_type.value} (UID: {self.uid or 'Unknown'})"

class CardTypeDetector:
    """Utility class for detecting MIFARE card types"""
    
    # ATR patterns for different card types
    ATR_PATTERNS = {
        MifareCardType.CLASSIC_1K: [
            "3B8F8001804F0CA000000306030001000000006A",
            "3B8F8001804F0CA0000003060300010000000068"
        ],
        MifareCardType.CLASSIC_4K: [
            "3B8F8001804F0CA000000306030002000000006B",
            "3B8F8001804F0CA0000003060300020000000069"
        ],
        MifareCardType.ULTRALIGHT: [
            "3B8080018080",
            "3B8F8001804F0CA000000306030003000000006C"
        ],
        MifareCardType.DESFIRE_EV1: [
            "3B8180018080",
            "3B8A80018080"
        ],
        MifareCardType.DESFIRE_EV2: [
            "3B8A80018080",
            "3B8180018080"
        ],
        MifareCardType.DESFIRE_EV3: [
            "3B8A80018080"
        ]
    }
    
    # Memory specifications for different card types
    CARD_SPECS = {
        MifareCardType.CLASSIC_1K: {
            'memory_size': 1024,
            'sector_count': 16,
            'block_count': 64,
            'block_size': 16
        },
        MifareCardType.CLASSIC_4K: {
            'memory_size': 4096,
            'sector_count': 40,
            'block_count': 256,
            'block_size': 16
        },
        MifareCardType.ULTRALIGHT: {
            'memory_size': 512,
            'sector_count': 0,
            'block_count': 16,
            'block_size': 4
        },
        MifareCardType.ULTRALIGHT_C: {
            'memory_size': 1536,
            'sector_count': 0,
            'block_count': 48,
            'block_size': 4
        },
        MifareCardType.DESFIRE_EV1: {
            'memory_size': 8192,  # Variable, this is common size
            'sector_count': 0,
            'block_count': 0,
            'block_size': 0
        },
        MifareCardType.DESFIRE_EV2: {
            'memory_size': 8192,  # Variable
            'sector_count': 0,
            'block_count': 0,
            'block_size': 0
        },
        MifareCardType.DESFIRE_EV3: {
            'memory_size': 8192,  # Variable
            'sector_count': 0,
            'block_count': 0,
            'block_size': 0
        }
    }
    
    @classmethod
    def detect_card_type(cls, atr: str, uid: Optional[str] = None) -> MifareCardType:
        """Detect card type based on ATR and optionally UID"""
        atr_clean = atr.upper().replace(' ', '')
        
        # Check ATR patterns
        for card_type, patterns in cls.ATR_PATTERNS.items():
            for pattern in patterns:
                if pattern.upper() in atr_clean:
                    return card_type
        
        # If no exact match, try partial matching
        if '3B8F8001804F0CA00000030603' in atr_clean:
            return MifareCardType.CLASSIC_1K  # Default to 1K for Classic variants
        elif '3B8080' in atr_clean:
            return MifareCardType.ULTRALIGHT
        elif '3B8180' in atr_clean or '3B8A80' in atr_clean:
            return MifareCardType.DESFIRE_EV2  # Default to EV2 for DESFire variants
        
        return MifareCardType.UNKNOWN
    
    @classmethod
    def get_card_specs(cls, card_type: MifareCardType) -> Dict[str, Any]:
        """Get specifications for a card type"""
        return cls.CARD_SPECS.get(card_type, {})
    
    @classmethod
    def create_card_info(cls, atr: str, uid: Optional[str] = None, 
                        reader: Optional[str] = None) -> CardInfo:
        """Create CardInfo object from ATR and other data"""
        card_type = cls.detect_card_type(atr, uid)
        specs = cls.get_card_specs(card_type)
        
        return CardInfo(
            card_type=card_type,
            uid=uid,
            atr=atr,
            reader=reader,
            memory_size=specs.get('memory_size'),
            sector_count=specs.get('sector_count'),
            block_count=specs.get('block_count')
        )
