#!/usr/bin/env python3
"""
Mifare App - Main Application Entry Point

A Python application for working with MIFARE cards and NFC operations.
"""

import sys
import click
from colorama import init, Fore, Style

# Initialize colorama for cross-platform colored output
init()

from mifare import CardReader, MifareUtils

@click.group()
@click.version_option(version='1.0.0')
def cli():
    """Mifare App - MIFARE card management tool"""
    pass

@cli.command()
def scan():
    """Scan for available MIFARE cards"""
    click.echo(f"{Fore.CYAN}Scanning for MIFARE cards...{Style.RESET_ALL}")
    
    try:
        reader = CardReader()
        cards = reader.scan_cards()
        
        if cards:
            click.echo(f"{Fore.GREEN}Found {len(cards)} card(s):{Style.RESET_ALL}")
            for i, card in enumerate(cards, 1):
                click.echo(f"  {i}. {card}")
        else:
            click.echo(f"{Fore.YELLOW}No cards detected{Style.RESET_ALL}")
            
    except Exception as e:
        click.echo(f"{Fore.RED}Error: {e}{Style.RESET_ALL}")

@cli.command()
@click.option('--card-id', help='Specific card ID to read')
def read(card_id):
    """Read data from a MIFARE card"""
    click.echo(f"{Fore.CYAN}Reading MIFARE card...{Style.RESET_ALL}")
    
    try:
        reader = CardReader()
        data = reader.read_card(card_id)
        
        if data:
            click.echo(f"{Fore.GREEN}Card data:{Style.RESET_ALL}")
            MifareUtils.display_card_data(data)
        else:
            click.echo(f"{Fore.YELLOW}No data found{Style.RESET_ALL}")
            
    except Exception as e:
        click.echo(f"{Fore.RED}Error: {e}{Style.RESET_ALL}")

@cli.command()
def info():
    """Display information about connected card readers"""
    click.echo(f"{Fore.CYAN}Card Reader Information:{Style.RESET_ALL}")
    
    try:
        reader = CardReader()
        readers = reader.list_readers()
        
        if readers:
            for i, reader_name in enumerate(readers, 1):
                click.echo(f"  {i}. {reader_name}")
        else:
            click.echo(f"{Fore.YELLOW}No card readers found{Style.RESET_ALL}")
            
    except Exception as e:
        click.echo(f"{Fore.RED}Error: {e}{Style.RESET_ALL}")

def main():
    """Main entry point"""
    try:
        cli()
    except KeyboardInterrupt:
        click.echo(f"\n{Fore.YELLOW}Operation cancelled by user{Style.RESET_ALL}")
        sys.exit(1)
    except Exception as e:
        click.echo(f"{Fore.RED}Unexpected error: {e}{Style.RESET_ALL}")
        sys.exit(1)

if __name__ == '__main__':
    main()
